/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.spring.factory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.context.event.config.EventPublishingConfigService;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosConfigListenerExecutorIfPresent;
import static com.alibaba.nacos.spring.util.NacosUtils.identify;

/**
 * Cacheable Event Publishing {@link NacosServiceFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@SuppressWarnings("unchecked")
public class CacheableEventPublishingNacosServiceFactory implements NacosServiceFactory {

	private static volatile CacheableEventPublishingNacosServiceFactory SINGLETON = new CacheableEventPublishingNacosServiceFactory();

	private final Map<String, ConfigService> configServicesCache = new LinkedHashMap<String, ConfigService>(
			2);

	private final Map<String, NamingService> namingServicesCache = new LinkedHashMap<String, NamingService>(
			2);

	private final Map<String, NamingMaintainService> maintainServiceCache = new LinkedHashMap<String, NamingMaintainService>(
			2);

	private final LinkedList<DeferServiceHolder> deferServiceCache = new LinkedList<DeferServiceHolder>();

	private ConfigurableApplicationContext context;

	private ExecutorService nacosConfigListenerExecutor;

	private Map<ServiceType, AbstractCreateWorker> createWorkerManager = new HashMap<ServiceType, AbstractCreateWorker>(
			3);

	public CacheableEventPublishingNacosServiceFactory() {
		createWorkerManager.put(ServiceType.CONFIG, new ConfigCreateWorker());
		createWorkerManager.put(ServiceType.NAMING, new NamingCreateWorker());
		createWorkerManager.put(ServiceType.MAINTAIN, new MaintainCreateWorker());
		createWorkerManager = Collections.unmodifiableMap(createWorkerManager);
	}

	public static CacheableEventPublishingNacosServiceFactory getSingleton() {
		return SINGLETON;
	}

	@Override
	public ConfigService createConfigService(Properties properties)
			throws NacosException {
		Properties copy = new Properties();
		copy.putAll(properties);
		return (ConfigService) createWorkerManager.get(ServiceType.CONFIG).run(copy,
				null);
	}

	@Override
	public NamingService createNamingService(Properties properties)
			throws NacosException {
		Properties copy = new Properties();
		copy.putAll(properties);
		return (NamingService) createWorkerManager.get(ServiceType.NAMING).run(copy,
				null);
	}

	// Exist some cases need to create the ConfigService | NamingService |
	// NamingMaintainService
	// before loading the Context object, lazy loading

	@Override
	public NamingMaintainService createNamingMaintainService(Properties properties)
			throws NacosException {
		Properties copy = new Properties();
		copy.putAll(properties);
		return (NamingMaintainService) createWorkerManager.get(ServiceType.MAINTAIN)
				.run(copy, null);
	}

	public <T> T deferCreateService(T service, Properties properties) {
		DeferServiceHolder serviceHolder = new DeferServiceHolder();
		serviceHolder.setHolder(service);
		serviceHolder.setProperties(properties);
		deferServiceCache.add(serviceHolder);
		return service;
	}

	@SuppressWarnings("unchecked")
	public void publishDeferService(ApplicationContext context) throws NacosException {
		setApplicationContext(context);
		for (DeferServiceHolder holder : deferServiceCache) {
			final Object o = holder.getHolder();
			final Properties properties = holder.getProperties();
			if (o instanceof ConfigService) {
				ConfigService configService = (ConfigService) o;
				createWorkerManager.get(ServiceType.CONFIG).run(properties,
						configService);
			}
			else if (o instanceof NamingService) {
				NamingService namingService = (NamingService) o;
				createWorkerManager.get(ServiceType.NAMING).run(properties,
						namingService);
			}
			else if (o instanceof NamingMaintainService) {
				NamingMaintainService maintainService = (NamingMaintainService) o;
				createWorkerManager.get(ServiceType.MAINTAIN).run(properties,
						maintainService);
			}
		}
		deferServiceCache.clear();
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = (ConfigurableApplicationContext) applicationContext;
		this.nacosConfigListenerExecutor = getSingleton().nacosConfigListenerExecutor == null
				? getNacosConfigListenerExecutorIfPresent(applicationContext)
				: getSingleton().nacosConfigListenerExecutor;
	}

	@Override
	public Collection<ConfigService> getConfigServices() {
		return configServicesCache.values();
	}

	@Override
	public Collection<NamingService> getNamingServices() {
		return namingServicesCache.values();
	}

	@Override
	public Collection<NamingMaintainService> getNamingMaintainService() {
		return maintainServiceCache.values();
	}

	private static enum ServiceType {

		/**
		 * Config
		 */
		CONFIG,

		/**
		 * Naming
		 */
		NAMING,

		/**
		 * Maintain
		 */
		MAINTAIN

	}

	static class DeferServiceHolder {

		private Properties properties;
		private Object holder;
		private ServiceType type;

		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

		Object getHolder() {
			return holder;
		}

		void setHolder(Object holder) {
			this.holder = holder;
		}

		public ServiceType getType() {
			return type;
		}

		public void setType(ServiceType type) {
			this.type = type;
		}
	}

	abstract static class AbstractCreateWorker<T> {

		AbstractCreateWorker() {
		}

		/**
		 * To perform the corresponding create and logic object cache
		 *
		 * @param properties Set the parameters
		 * @param service nacos service {ConfigService | NamingService |
		 *     NamingMaintainService}
		 * @return T service
		 * @throws NacosException
		 */
		public abstract T run(Properties properties, T service) throws NacosException;

	}

	class ConfigCreateWorker extends AbstractCreateWorker<ConfigService> {

		ConfigCreateWorker() {
		}

		@Override
		public ConfigService run(Properties properties, ConfigService service)
				throws NacosException {
			String cacheKey = identify(properties);
			ConfigService configService = configServicesCache.get(cacheKey);

			if (configService == null) {
				if (service == null) {
					service = NacosFactory.createConfigService(properties);
				}
				configService = new EventPublishingConfigService(service, properties,
						getSingleton().context,
						getSingleton().nacosConfigListenerExecutor);
				configServicesCache.put(cacheKey, configService);
			}
			return configService;
		}
	}

	class NamingCreateWorker extends AbstractCreateWorker<NamingService> {

		NamingCreateWorker() {
		}

		@Override
		public NamingService run(Properties properties, NamingService service)
				throws NacosException {
			String cacheKey = identify(properties);
			NamingService namingService = namingServicesCache.get(cacheKey);

			if (namingService == null) {
				if (service == null) {
					service = NacosFactory.createNamingService(properties);
				}
				namingService = new DelegatingNamingService(service, properties);
				namingServicesCache.put(cacheKey, namingService);
			}
			return namingService;
		}
	}

	class MaintainCreateWorker extends AbstractCreateWorker<NamingMaintainService> {

		MaintainCreateWorker() {
		}

		@Override
		public NamingMaintainService run(Properties properties,
				NamingMaintainService service) throws NacosException {
			String cacheKey = identify(properties);
			NamingMaintainService namingMaintainService = maintainServiceCache
					.get(cacheKey);

			if (namingMaintainService == null) {
				if (service == null) {
					service = NacosFactory.createMaintainService(properties);
				}
				namingMaintainService = new DelegatingNamingMaintainService(service,
						properties);
				maintainServiceCache.put(cacheKey, namingMaintainService);
			}
			return namingMaintainService;
		}
	}

}
