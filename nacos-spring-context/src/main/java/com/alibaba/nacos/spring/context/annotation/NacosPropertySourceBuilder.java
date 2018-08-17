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
package com.alibaba.nacos.spring.context.annotation;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.event.NacosConfigReceiveEvent;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.NacosConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosServiceFactoryBean;
import static com.alibaba.nacos.spring.util.NacosUtils.buildDefaultPropertySourceName;
import static com.alibaba.nacos.spring.util.NacosUtils.toProperties;

/**
 * Nacos {@link PropertySource} Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
class NacosPropertySourceBuilder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    private String dataId;

    private String groupId;

    private boolean autoRefreshed;

    private Properties properties;

    private ConfigurableEnvironment environment;

    private BeanFactory beanFactory;

    private ApplicationEventPublisher applicationEventPublisher;

    public NacosPropertySourceBuilder name(String name) {
        this.name = name;
        return this;
    }

    public NacosPropertySourceBuilder dataId(String dataId) {
        this.dataId = dataId;
        return this;
    }

    public NacosPropertySourceBuilder groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public NacosPropertySourceBuilder autoRefreshed(boolean autoRefreshed) {
        this.autoRefreshed = autoRefreshed;
        return this;
    }

    public NacosPropertySourceBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public NacosPropertySourceBuilder environment(ConfigurableEnvironment environment) {
        this.environment = environment;
        return this;
    }

    public NacosPropertySourceBuilder beanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        return this;
    }

    public NacosPropertySourceBuilder applicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        return this;
    }

    /**
     * Build Nacos {@link PropertySource}
     *
     * @return if Nacos config is absent , return <code>null</code>
     */
    public PropertySource build() {

        NacosConfigLoader loader = new NacosConfigLoader(environment);

        NacosServiceFactory nacosServiceFactory = getNacosServiceFactoryBean(beanFactory);

        loader.setNacosServiceFactory(nacosServiceFactory);

        String config = loader.load(dataId, groupId, properties);

        if (autoRefreshed) {
            addListener(loader);
        }

        if (!StringUtils.hasText(config)) {
            if (logger.isWarnEnabled()) {
                logger.warn("There is no content for Nacos PropertySource from dataId : " + dataId + " , groupId : "
                        + groupId);
            }
            return null;
        }

        Properties properties = toProperties(config);

        if (!StringUtils.hasText(name)) {
            name = buildDefaultPropertySourceName(dataId, groupId, properties);
        }

        return new PropertiesPropertySource(name, properties);
    }

    private void addListener(NacosConfigLoader loader) {
        try {
            final ConfigService configService = loader.getConfigService();
            configService.addListener(dataId, groupId, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    if (applicationEventPublisher != null) {
                        applicationEventPublisher.publishEvent(
                            new NacosConfigReceiveEvent(configService, dataId, groupId, configInfo));
                    }
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException("ConfigService can't add Listener with dataId :"
                + dataId + " , groupId : " + groupId + " , properties : " + properties
                , e);
        }
    }

}
