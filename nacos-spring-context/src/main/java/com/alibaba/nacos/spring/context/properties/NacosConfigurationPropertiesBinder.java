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
package com.alibaba.nacos.spring.context.properties;

import com.alibaba.nacos.api.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.annotation.NacosIgnore;
import com.alibaba.nacos.api.annotation.NacosProperty;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.DataBinder;

import java.lang.reflect.Field;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosUtils.getContent;
import static com.alibaba.nacos.spring.util.NacosUtils.toProperties;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link NacosConfigurationProperties} Bean Binder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigurationPropertiesBinder {

    private static final Logger logger = LoggerFactory.getLogger(NacosConfigurationPropertiesBinder.class);

    private final ConfigService configService;

    public NacosConfigurationPropertiesBinder(ConfigService configService) {
        Assert.notNull(configService, "ConfigService must not be null!");
        this.configService = configService;
    }

    public void bind(Object bean) {

        NacosConfigurationProperties properties = findAnnotation(bean.getClass(), NacosConfigurationProperties.class);

        bind(bean, properties);

    }

    public void bind(final Object bean, final NacosConfigurationProperties properties) {

        Assert.notNull(bean, "Bean must not be null!");

        Assert.notNull(properties, "NacosConfigurationProperties must not be null!");

        String dataId = properties.dataId();

        String groupId = properties.groupId();

        if (properties.autoRefreshed()) { // Add a Listener if auto-refreshed

            try {
                configService.addListener(dataId, groupId, new AbstractListener() {
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        doBind(bean, properties, configInfo);
                    }
                });
            } catch (NacosException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        String content = getContent(configService, dataId, groupId);

        if (hasText(content)) {
            doBind(bean, properties, content);
        }
    }

    protected void doBind(Object bean, final NacosConfigurationProperties properties, String content) {
        Properties configProperties = toProperties(content);
        PropertyValues propertyValues = resolvePropertyValues(bean, content);
        doBind(bean, properties, propertyValues);
    }

    private void doBind(Object bean, NacosConfigurationProperties properties,
                        PropertyValues propertyValues) {
        DataBinder dataBinder = new DataBinder(bean);
        dataBinder.setAutoGrowNestedPaths(properties.ignoreNestedProperties());
        dataBinder.setIgnoreInvalidFields(properties.ignoreInvalidFields());
        dataBinder.setIgnoreUnknownFields(properties.ignoreUnknownFields());
        dataBinder.bind(propertyValues);
    }

    private PropertyValues resolvePropertyValues(Object bean, String content) {
        final Properties configProperties = toProperties(content);
        final MutablePropertyValues propertyValues = new MutablePropertyValues();
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                String propertyName = resolvePropertyName(field);
                if (hasText(propertyName) && configProperties.containsKey(propertyName)) {
                    String propertyValue = configProperties.getProperty(propertyName);
                    propertyValues.add(field.getName(), propertyValue);
                }
            }
        });
        return propertyValues;
    }

    private String resolvePropertyName(Field field) {
        // Ignore property name if @NacosIgnore present
        if (getAnnotation(field, NacosIgnore.class) != null) {
            return null;
        }
        NacosProperty nacosProperty = getAnnotation(field, NacosProperty.class);
        // If @NacosProperty present ,return its value() , or field name
        return nacosProperty != null ? nacosProperty.value() : field.getName();
    }

}
