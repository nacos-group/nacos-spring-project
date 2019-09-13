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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.context.event.config.EventPublishingConfigService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosConfigListenerExecutorIfPresent;
import static com.alibaba.nacos.spring.util.NacosUtils.identify;

/**
 * Cacheable Event Publishing {@link NacosServiceFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class CacheableEventPublishingNacosServiceFactory implements NacosServiceFactory {

    private static final CacheableEventPublishingNacosServiceFactory SINGLETON = new CacheableEventPublishingNacosServiceFactory();

    private final Map<String, ConfigService> configServicesCache = new LinkedHashMap<String, ConfigService>(2);

    private final Map<String, NamingService> namingServicesCache = new LinkedHashMap<String, NamingService>(2);

    private final Map<String, NamingMaintainService> maintainServiceCache = new LinkedHashMap<String, NamingMaintainService>(2);

    private final LinkedList<DeferServiceHolder> deferServiceCache = new LinkedList<DeferServiceHolder>();

    private ConfigurableApplicationContext context;

    private ExecutorService nacosConfigListenerExecutor;

    @Override
    public ConfigService createConfigService(Properties properties) throws NacosException {
        Properties copy = new Properties();
        copy.putAll(properties);
        return new ConfigCreateWorker(copy, null).run();
    }

    @Override
    public NamingService createNamingService(Properties properties) throws NacosException {
        Properties copy = new Properties();
        copy.putAll(properties);
        return new NamingCreateWorker(copy, null).run();
    }

    @Override
    public NamingMaintainService createNamingMaintainService(Properties properties) throws NacosException {
        Properties copy = new Properties();
        copy.putAll(properties);
        return new MaintainCreateWorker(copy, null).run();
    }

    // Exist some cases need to create the ConfigService | NamingService | NamingMaintainService
    // before loading the Context object, lazy loading

    public <T> T deferCreateService(T service, Properties properties) {
        DeferServiceHolder serviceHolder = new DeferServiceHolder();
        serviceHolder.setHolder(service);
        serviceHolder.setProperties(properties);
        deferServiceCache.add(serviceHolder);
        return service;
    }

    public void publishDeferService() throws NacosException {
        AbstractCreateWorker configCreateWorkerReuse = new ConfigCreateWorker();
        AbstractCreateWorker namingCreateWorkerReuse = new NamingCreateWorker();
        AbstractCreateWorker maintainCreateWorkerReuse = new MaintainCreateWorker();
        for (DeferServiceHolder holder : deferServiceCache) {
            final Object o = holder.getHolder();
            final Properties properties = holder.getProperties();
            if (o instanceof ConfigService) {
                ConfigService configService = (ConfigService) o;
                configCreateWorkerReuse.setProperties(properties);
                configCreateWorkerReuse.setService(configService);
                configCreateWorkerReuse.run();
                configCreateWorkerReuse.clean();
            } else if (o instanceof NamingService) {
                NamingService namingService = (NamingService) o;
                namingCreateWorkerReuse.setProperties(properties);
                namingCreateWorkerReuse.setService(namingService);
                namingCreateWorkerReuse.run();
                namingCreateWorkerReuse.clean();
            } else if (o instanceof NamingMaintainService) {
                NamingMaintainService maintainService = (NamingMaintainService) o;
                maintainCreateWorkerReuse.setProperties(properties);
                maintainCreateWorkerReuse.setService(maintainService);
                maintainCreateWorkerReuse.run();
                maintainCreateWorkerReuse.clean();
            }
        }
        deferServiceCache.clear();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
        this.nacosConfigListenerExecutor = getSingleton().nacosConfigListenerExecutor == null ?
                getNacosConfigListenerExecutorIfPresent(applicationContext) : getSingleton().nacosConfigListenerExecutor;
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

    public static CacheableEventPublishingNacosServiceFactory getSingleton() {
        return SINGLETON;
    }

    static class DeferServiceHolder {

        private Properties properties;
        private Object holder;

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public Object getHolder() {
            return holder;
        }

        public void setHolder(Object holder) {
            this.holder = holder;
        }
    }

    abstract class AbstractCreateWorker<T> {

        protected Properties properties;

        protected T service;

        AbstractCreateWorker() {
        }

        AbstractCreateWorker(Properties properties, T service) {
            this.properties = properties;
            this.service = service;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public void setService(T service) {
            this.service = service;
        }

        String cacheKey() {
            return identify(properties);
        }

        /**
         * To perform the corresponding create and logic object cache
         *
         * @return T service
         * @throws NacosException
         */
        public abstract T run() throws NacosException;

        void clean() {
            properties = null;
            service = null;
        }
    }

    class ConfigCreateWorker extends AbstractCreateWorker<ConfigService> {

        ConfigCreateWorker() {
        }

        ConfigCreateWorker(Properties properties, ConfigService service) {
            super(properties, service);
        }

        @Override
        public ConfigService run() throws NacosException {
            String cacheKey = cacheKey();
            ConfigService configService = configServicesCache.get(cacheKey);

            if (configService == null) {
                if (service == null) {
                    service = NacosFactory.createConfigService(properties);
                }
                configService = new EventPublishingConfigService(service, properties, getSingleton().context,
                        getSingleton().nacosConfigListenerExecutor);
                configServicesCache.put(cacheKey, configService);
            }
            return configService;
        }
    }

    class NamingCreateWorker extends AbstractCreateWorker<NamingService> {

        NamingCreateWorker() {
        }

        NamingCreateWorker(Properties properties, NamingService service) {
            super(properties, service);
        }

        @Override
        public NamingService run() throws NacosException {
            String cacheKey = cacheKey();
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

        MaintainCreateWorker(Properties properties, NamingMaintainService service) {
            super(properties, service);
        }

        @Override
        public NamingMaintainService run() throws NacosException {
            String cacheKey = cacheKey();
            NamingMaintainService namingMaintainService = maintainServiceCache.get(cacheKey);

            if (namingMaintainService == null) {
                if (service == null) {
                    service = NacosFactory.createMaintainService(properties);
                }
                namingMaintainService = new DelegatingNamingMaintainService(service, properties);
                maintainServiceCache.put(cacheKey, namingMaintainService);
            }
            return namingMaintainService;
        }
    }

}
