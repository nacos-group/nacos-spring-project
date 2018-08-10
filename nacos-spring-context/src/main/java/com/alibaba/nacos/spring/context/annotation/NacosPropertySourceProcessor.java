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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.context.annotation.NacosPropertySource.*;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.getGlobalPropertiesBean;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_STRING_ATTRIBUTE_VALUE;
import static com.alibaba.nacos.spring.util.NacosUtils.resolveProperties;
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

    public NacosPropertySourceProcessor(BeanFactory beanFactory, ConfigurableEnvironment environment) {
        this.beanFactory = beanFactory;
        this.environment = environment;
        this.globalNacosProperties = getGlobalPropertiesBean(beanFactory);
    }

    public void process(Map<String, Object> attributes) {

        String name = (String) attributes.get(NAME_ATTRIBUTE_NAME);
        String dataId = (String) attributes.get(DATA_ID_ATTRIBUTE_NAME);
        String groupId = (String) attributes.get(GROUP_ID_ATTRIBUTE_NAME);

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
    }

    private void addPropertySource(PropertySource propertySource, Map<String, Object> nacosPropertySourceAttributes) {

        MutablePropertySources propertySources = environment.getPropertySources();

        boolean isFirst = Boolean.TRUE.equals(nacosPropertySourceAttributes.get(FIRST_ATTRIBUTE_NAME));
        String before = (String) nacosPropertySourceAttributes.get(BEFORE_ATTRIBUTE_NAME);
        String after = (String) nacosPropertySourceAttributes.get(AFTER_ATTRIBUTE_NAME);

        boolean hasBefore = !nullSafeEquals(DEFAULT_STRING_ATTRIBUTE_VALUE, before);
        boolean hasAfter = !nullSafeEquals(DEFAULT_STRING_ATTRIBUTE_VALUE, before);

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

}
