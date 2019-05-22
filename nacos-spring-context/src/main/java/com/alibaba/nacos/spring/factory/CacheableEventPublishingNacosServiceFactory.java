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
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.nacos.spring.context.constants.NacosConstants.DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM;
import static com.alibaba.nacos.spring.context.constants.NacosConstants.NACOS_CONFIG_LISTENER_PARALLELISM;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosConfigListenerExecutorIfPresent;
import static com.alibaba.nacos.spring.util.NacosUtils.identify;

/**
 * Cacheable Event Publishing {@link NacosServiceFactory}
 *
 * Remove the object from the spring container for a singleton
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @author liaochuntao
 * @since 0.1.0
 */
public class CacheableEventPublishingNacosServiceFactory implements NacosServiceFactory, ApplicationContextAware {

    private static final CacheableEventPublishingNacosServiceFactory singleton = new CacheableEventPublishingNacosServiceFactory();;

    private final Map<String, ConfigService> configServicesCache = new LinkedHashMap<String, ConfigService>(2);

    private final Map<String, NamingService> namingServicesCache = new LinkedHashMap<String, NamingService>(2);

    private final Map<String, NamingMaintainService> maintainServicesCache = new LinkedHashMap<String, NamingMaintainService>(2);

    private ConfigurableApplicationContext context;

    private ExecutorService nacosConfigListenerExecutor;

    @Override
    public ConfigService createConfigService(Properties properties) throws NacosException {

        Properties copy = new Properties();

        copy.putAll(properties);

        // 根据配置信息创建一个key用于缓存 ConfigService
        String cacheKey = identify(copy);

        ConfigService configService;
        synchronized (this) {

            configService = configServicesCache.get(cacheKey);

            if (configService == null) {
                configService = doCreateConfigService(copy);
                configServicesCache.put(cacheKey, configService);
            }
        }

        return configService;
    }

    private ConfigService doCreateConfigService(Properties properties) throws NacosException {
        ConfigService configService = NacosFactory.createConfigService(properties);
        return new EventPublishingConfigService(configService, properties, context, nacosConfigListenerExecutor);
    }

    @Override
    public NamingService createNamingService(Properties properties) throws NacosException {

        Properties copy = new Properties();

        copy.putAll(properties);

        String cacheKey = identify(copy);

        NamingService namingService;
        synchronized (this) {

            namingService = namingServicesCache.get(cacheKey);

            if (namingService == null) {
                namingService = new DelegatingNamingService(NacosFactory.createNamingService(copy), properties);
                namingServicesCache.put(cacheKey, namingService);
            }
        }

        return namingService;
    }

    @Override
    public NamingMaintainService createNamingMaintainService(Properties properties) throws NacosException {
        Properties copy = new Properties();

        copy.putAll(properties);

        String cacheKey = identify(copy);

        NamingMaintainService maintainService;

        synchronized (this) {
            maintainService = maintainServicesCache.get(cacheKey);

            if (maintainService == null) {
                maintainService = new DelegatingNamingMaintainService(NacosFactory.createMaintainService(properties), properties);
                maintainServicesCache.put(cacheKey, maintainService);
            }
        }

        return maintainService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
        this.nacosConfigListenerExecutor = buildExecutorService(applicationContext.getEnvironment());
    }

    @Override
    public Collection<ConfigService> getConfigServices() {
        return configServicesCache.values();
    }

    @Override
    public Collection<NamingService> getNamingServices() {
        return namingServicesCache.values();
    }

    public static CacheableEventPublishingNacosServiceFactory getSingleton() {
        return singleton;
    }

    private ExecutorService buildExecutorService(Environment environment) {
        int parallelism = getParallelism(environment);
        return Executors.newFixedThreadPool(parallelism, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("NacosConfigListener-ThreadPool-" + threadNumber.getAndIncrement());
                return thread;
            }
        });
    }

    private static int getParallelism(Environment environment) {
        int parallelism = environment.getProperty(NACOS_CONFIG_LISTENER_PARALLELISM, int.class,
                DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM);
        return parallelism < 1 ? DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM : parallelism;
    }
}
