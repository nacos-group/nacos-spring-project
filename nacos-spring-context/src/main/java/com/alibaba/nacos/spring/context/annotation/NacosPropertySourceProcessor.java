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
import com.alibaba.nacos.spring.context.config.NacosPropertySourceBeanDefinitionParser;
import com.alibaba.nacos.spring.context.event.NacosConfigReceivedEvent;
import com.alibaba.nacos.spring.util.NacosConfigLoader;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.context.annotation.NacosPropertySource.*;
import static com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource.CONFIG;
import static com.alibaba.nacos.spring.util.NacosUtils.*;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * Nacos {@link PropertySource} Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @see NacosPropertySourcePostProcessor
 * @see NacosPropertySourceBeanDefinitionParser
 * @since 0.1.0
 */
class NacosPropertySourceProcessor {

    private final Properties globalNacosProperties;

    private final ConfigurableEnvironment environment;

    private final BeanFactory beanFactory;

    private ApplicationEventPublisher applicationEventPublisher;

    NacosPropertySourceProcessor(BeanFactory beanFactory, ConfigurableEnvironment environment,
                                 ApplicationEventPublisher applicationEventPublisher) {
        this.beanFactory = beanFactory;
        this.environment = environment;
        this.globalNacosProperties = CONFIG.getMergedGlobalProperties(beanFactory);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void process(Map<String, Object> attributes) {

        if (CollectionUtils.isEmpty(attributes)) {
            return;
        }

        String name = (String) attributes.get(NAME_ATTRIBUTE_NAME);
        String dataId = (String) attributes.get(DATA_ID_ATTRIBUTE_NAME);
        String groupId = (String) attributes.get(GROUP_ID_ATTRIBUTE_NAME);
        boolean autoRefreshed = Boolean.TRUE.equals(attributes.get(AUTO_REFRESHED));

        Map<String, Object> properties = (Map<String, Object>) attributes.get(PROPERTIES_ATTRIBUTE_NAME);

        Properties nacosProperties = resolveProperties(properties, environment, globalNacosProperties);

        NacosPropertySourceBuilder builder = new NacosPropertySourceBuilder();

        builder.name(name)
                .dataId(dataId)
                .groupId(groupId)
                .properties(nacosProperties)
                .environment(environment)
                .beanFactory(beanFactory);

        addPropertySource(builder.build(), attributes);

        if (autoRefreshed) {
            addListener(builder.getNacosConfigLoader(), attributes);
        }
    }

    private void addPropertySource(PropertySource propertySource, Map<String, Object> nacosPropertySourceAttributes) {

        if (propertySource == null) {
            return;
        }

        MutablePropertySources propertySources = environment.getPropertySources();

        boolean isFirst = Boolean.TRUE.equals(nacosPropertySourceAttributes.get(FIRST_ATTRIBUTE_NAME));
        String before = (String) nacosPropertySourceAttributes.get(BEFORE_ATTRIBUTE_NAME);
        String after = (String) nacosPropertySourceAttributes.get(AFTER_ATTRIBUTE_NAME);

        boolean hasBefore = !nullSafeEquals(DEFAULT_STRING_ATTRIBUTE_VALUE, before);
        boolean hasAfter = !nullSafeEquals(DEFAULT_STRING_ATTRIBUTE_VALUE, after);

        boolean isRelative = hasBefore || hasAfter;

        if (isFirst) { // If First
            propertySources.addFirst(propertySource);
        } else if (isRelative) { // If relative
            if (hasBefore) {
                propertySources.addBefore(before, propertySource);
            }
            if (hasAfter) {
                propertySources.addAfter(after, propertySource);
            }
        } else {
            propertySources.addLast(propertySource); // default add last
        }
    }

    private void addListener(NacosConfigLoader loader, final Map<String, Object> attributes) {
        final String name = (String) attributes.get(NAME_ATTRIBUTE_NAME);
        final String dataId = (String) attributes.get(DATA_ID_ATTRIBUTE_NAME);
        final String groupId = (String) attributes.get(GROUP_ID_ATTRIBUTE_NAME);
        try {
            final ConfigService configService = loader.getConfigService();
            configService.addListener(dataId, groupId, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    Properties properties = toProperties(configInfo);
                    String currentName = name;
                    if (!StringUtils.hasText(currentName)) {
                        currentName = buildDefaultPropertySourceName(dataId, groupId, properties);
                    }
                    addPropertySource(new PropertiesPropertySource(currentName, properties), attributes);

                    if (applicationEventPublisher != null) {
                        applicationEventPublisher.publishEvent(
                                new NacosConfigReceivedEvent(configService, dataId, groupId, configInfo));
                    }
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException("ConfigService can't add Listener with attributes : " + attributes, e);
        }
    }

}
