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
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.context.event.EventPublishingConfigService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosUtils.identify;

/**
 * Cacheable Event Publishing {@link NacosServiceFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class CacheableEventPublishingNacosServiceFactory implements NacosServiceFactory, ApplicationEventPublisherAware {

    private Map<String, ConfigService> configServicesCache = new HashMap<String, ConfigService>(2);

    private Map<String, NamingService> namingServicesCache = new HashMap<String, NamingService>(2);

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ConfigService createConfigService(Properties properties) throws NacosException {

        Properties copy = new Properties();

        copy.putAll(properties);

        String cacheKey = identify(copy);

        ConfigService configService = configServicesCache.get(cacheKey);

        if (configService == null) {
            configService = doCreateConfigService(copy);
            configServicesCache.put(cacheKey, configService);
        }

        return configService;
    }

    private ConfigService doCreateConfigService(Properties properties) throws NacosException {
        ConfigService configService = NacosFactory.createConfigService(properties);
        return applicationEventPublisher == null ? configService :
                new EventPublishingConfigService(configService, applicationEventPublisher);
    }

    @Override
    public NamingService createNamingService(Properties properties) throws NacosException {

        Properties copy = new Properties();

        copy.putAll(properties);

        String cacheKey = identify(copy);

        NamingService namingService = namingServicesCache.get(cacheKey);

        if (namingService == null) {
            namingService = NacosFactory.createNamingService(copy);
            namingServicesCache.put(cacheKey, namingService);
        }

        return namingService;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
