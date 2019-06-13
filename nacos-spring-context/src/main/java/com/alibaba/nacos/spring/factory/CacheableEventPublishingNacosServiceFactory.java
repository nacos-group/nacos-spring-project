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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosConfigListenerExecutorIfPresent;
import static com.alibaba.nacos.spring.util.NacosUtils.identify;

/**
 * Cacheable Event Publishing {@link NacosServiceFactory}
 * <p>
 * Remove the object from the spring container for a SINGLETON
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @author liaochuntao
 * @since 0.1.0
 */
public class CacheableEventPublishingNacosServiceFactory implements NacosServiceFactory {

    private static final CacheableEventPublishingNacosServiceFactory SINGLETON = new CacheableEventPublishingNacosServiceFactory();

    private final Map<String, ConfigService> configServicesCache = new LinkedHashMap<String, ConfigService>(2);

    private final Map<String, NamingService> namingServicesCache = new LinkedHashMap<String, NamingService>(2);

    private final Map<String, NamingMaintainService> maintainServicesCache = new LinkedHashMap<String, NamingMaintainService>(2);

    private ConfigurableApplicationContext context;

    private ExecutorService nacosConfigListenerExecutor;

    @Override
    public ConfigService createConfigService(Properties properties) throws NacosException {

        Properties copy = new Properties();

        copy.putAll(properties);

        String cacheKey = identify(copy);

        ConfigService configService;

        configService = configServicesCache.get(cacheKey);

        if (configService == null) {
            configService = doCreateConfigService(copy);
            configServicesCache.put(cacheKey, configService);
        }

        return configService;
    }

    private ConfigService doCreateConfigService(Properties properties) throws NacosException {
        ConfigService configService = NacosFactory.createConfigService(properties);
        return new EventPublishingConfigService(configService, properties, getSingleton().context, getSingleton().nacosConfigListenerExecutor);
    }

    @Override
    public NamingService createNamingService(Properties properties) throws NacosException {

        Properties copy = new Properties();

        copy.putAll(properties);

        String cacheKey = identify(copy);

        NamingService namingService;

        namingService = namingServicesCache.get(cacheKey);

        if (namingService == null) {
            namingService = new DelegatingNamingService(NacosFactory.createNamingService(copy), properties);
            namingServicesCache.put(cacheKey, namingService);
        }

        return namingService;
    }

    @Override
    public NamingMaintainService createNamingMaintainService(Properties properties) throws NacosException {
        Properties copy = new Properties();

        copy.putAll(properties);

        String cacheKey = identify(copy);

        NamingMaintainService maintainService;

        maintainService = maintainServicesCache.get(cacheKey);

        if (maintainService == null) {
            maintainService = new DelegatingNamingMaintainService(NacosFactory.createMaintainService(properties), properties);
            maintainServicesCache.put(cacheKey, maintainService);
        }

        return maintainService;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        getSingleton().context = (ConfigurableApplicationContext) applicationContext;
        getSingleton().nacosConfigListenerExecutor = getSingleton().nacosConfigListenerExecutor == null ?
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

    public static CacheableEventPublishingNacosServiceFactory getSingleton() {
        return SINGLETON;
    }

}
