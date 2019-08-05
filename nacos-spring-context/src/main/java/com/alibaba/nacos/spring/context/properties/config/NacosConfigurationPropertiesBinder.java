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
package com.alibaba.nacos.spring.context.properties.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.config.annotation.NacosIgnore;
import com.alibaba.nacos.api.config.annotation.NacosProperty;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.context.event.config.EventPublishingConfigService;
import com.alibaba.nacos.spring.context.event.config.NacosConfigEvent;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import com.alibaba.nacos.spring.context.event.config.NacosConfigurationPropertiesBeanBoundEvent;
import com.alibaba.nacos.spring.util.NacosUtils;
import com.alibaba.nacos.spring.util.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.DataBinder;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getConfigServiceBeanBuilder;
import static com.alibaba.nacos.spring.util.NacosUtils.getContent;
import static com.alibaba.nacos.spring.util.NacosUtils.toProperties;
import static org.springframework.core.annotation.AnnotationUtils.*;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link NacosConfigurationProperties} Bean Binder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigurationPropertiesBinder {

    public static final String BEAN_NAME = "nacosConfigurationPropertiesBinder";

    private static final Logger logger = LoggerFactory.getLogger(NacosConfigurationPropertiesBinder.class);

    private final ConfigurableApplicationContext applicationContext;

    private final Environment environment;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final ConfigServiceBeanBuilder configServiceBeanBuilder;

    protected NacosConfigurationPropertiesBinder(ConfigurableApplicationContext applicationContext) {
        Assert.notNull(applicationContext, "ConfigurableApplicationContext must not be null!");
        this.applicationContext = applicationContext;
        this.environment = applicationContext.getEnvironment();
        this.applicationEventPublisher = applicationContext;
        this.configServiceBeanBuilder = getConfigServiceBeanBuilder(applicationContext);
    }

    protected void bind(Object bean, String beanName) {

        NacosConfigurationProperties properties = findAnnotation(bean.getClass(), NacosConfigurationProperties.class);

        bind(bean, beanName, properties);

    }

    protected void bind(final Object bean, final String beanName, final NacosConfigurationProperties properties) {

        Assert.notNull(bean, "Bean must not be null!");

        Assert.notNull(properties, "NacosConfigurationProperties must not be null!");

        // support read data-id and group-id from spring environment
        final String dataId = NacosUtils.readFromEnvironment(properties.dataId(), environment);
        final String groupId = NacosUtils.readFromEnvironment(properties.groupId(), environment);
        String fileType = NacosUtils.readTypeFromDataId(dataId);
        final String type = StringUtils.isEmpty(fileType) ? (properties.yaml() ? ConfigType.YAML.getType() : properties.type().getType()) : fileType;

        final ConfigService configService = configServiceBeanBuilder.build(properties.properties());

        // Add a Listener if auto-refreshed
        if (properties.autoRefreshed()) {

            Listener listener = new AbstractListener() {
                @Override
                public void receiveConfigInfo(String config) {
                    doBind(bean, beanName, dataId, groupId, type, properties, config, configService);
                }
            };
            try {//
                if (configService instanceof EventPublishingConfigService) {
                    ((EventPublishingConfigService) configService).addListener(dataId, groupId, type, listener);
                } else {
                    configService.addListener(dataId, groupId, listener);
                }
            } catch (NacosException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        String content = getContent(configService, dataId, groupId);

        if (hasText(content)) {
            doBind(bean, beanName, dataId, groupId, type, properties, content, configService);
        }
    }

    protected void doBind(Object bean, String beanName, String dataId, String groupId, String type,
                        NacosConfigurationProperties properties, String content, ConfigService configService) {
        final String prefix = properties.prefix();
        PropertyValues propertyValues = NacosUtils.resolvePropertyValues(bean, prefix, dataId, groupId, content, type);
        doBind(bean, properties, propertyValues);
        publishBoundEvent(bean, beanName, dataId, groupId, properties, content, configService);
        publishMetadataEvent(bean, beanName, dataId, groupId, properties);
    }

    protected void publishMetadataEvent(Object bean, String beanName, String dataId, String groupId,
                                      NacosConfigurationProperties properties) {

        NacosProperties nacosProperties = properties.properties();

        NacosConfigMetadataEvent metadataEvent = new NacosConfigMetadataEvent(properties);

        // Nacos Metadata
        metadataEvent.setDataId(dataId);
        metadataEvent.setGroupId(groupId);
        Properties resolvedNacosProperties = configServiceBeanBuilder.resolveProperties(nacosProperties);
        Map<String, Object> nacosPropertiesAttributes = getAnnotationAttributes(nacosProperties);
        metadataEvent.setNacosPropertiesAttributes(nacosPropertiesAttributes);
        metadataEvent.setNacosProperties(resolvedNacosProperties);

        // Bean Metadata
        Class<?> beanClass = bean.getClass();
        metadataEvent.setBeanName(beanName);
        metadataEvent.setBean(bean);
        metadataEvent.setBeanType(beanClass);
        metadataEvent.setAnnotatedElement(beanClass);

        // Publish event
        applicationEventPublisher.publishEvent(metadataEvent);
    }

    protected void publishBoundEvent(Object bean, String beanName, String dataId, String groupId,
                                   NacosConfigurationProperties properties, String content, ConfigService configService) {
        NacosConfigEvent event = new NacosConfigurationPropertiesBeanBoundEvent(configService, dataId, groupId, bean,
                beanName, properties, content);
        applicationEventPublisher.publishEvent(event);
    }

    private void doBind(Object bean, NacosConfigurationProperties properties,
                        PropertyValues propertyValues) {
        ObjectUtils.cleanMapOrCollectionField(bean);
        DataBinder dataBinder = new DataBinder(bean);
        dataBinder.setAutoGrowNestedPaths(properties.ignoreNestedProperties());
        dataBinder.setIgnoreInvalidFields(properties.ignoreInvalidFields());
        dataBinder.setIgnoreUnknownFields(properties.ignoreUnknownFields());
        dataBinder.bind(propertyValues);
    }

}
