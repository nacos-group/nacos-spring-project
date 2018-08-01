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

import com.alibaba.nacos.spring.util.NacosConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosConfigLoaderBean;
import static com.alibaba.nacos.spring.util.NacosUtils.*;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * {@link NacosPropertySource @NacosPropertySource} Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySourceProcessor implements BeanFactoryPostProcessor, EnvironmentAware, Ordered {

    /**
     * The bean name of {@link NacosPropertySourceProcessor}
     */
    public static final String BEAN_NAME = "nacosPropertySourceProcessor";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurableEnvironment environment;

    private NacosConfigLoader loader;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        this.loader = getNacosConfigLoaderBean(beanFactory);

        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            addPropertySource(beanName, beanFactory);
        }

    }

    private void addPropertySource(String beanName, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition instanceof AnnotatedBeanDefinition) {
            // @NacosPropertySource must be AnnotatedBeanDefinition
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            addPropertySource(beanName, annotatedBeanDefinition);
        }
    }

    private void addPropertySource(String beanName, AnnotatedBeanDefinition annotatedBeanDefinition) {

        AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();

        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(NacosPropertySource.class.getName());

        if (!CollectionUtils.isEmpty(annotationAttributes)) {
            addPropertySource(annotationAttributes);
        }

    }

    private void addPropertySource(Map<String, Object> nacosPropertySourceAttributes) {

        String name = (String) nacosPropertySourceAttributes.get("name");
        String dataId = (String) nacosPropertySourceAttributes.get("dataId");
        String groupId = (String) nacosPropertySourceAttributes.get("groupId");

        Map<String, Object> properties = (AnnotationAttributes) nacosPropertySourceAttributes.get("properties");

        Properties nacosProperties = resolveProperties(properties, environment);

        if (!StringUtils.hasText(name)) {
            name = buildDefaultPropertySourceName(dataId, groupId, nacosProperties);
        }

        String config = loader.load(dataId, groupId, nacosProperties);

        if (!StringUtils.hasText(config)) {
            if (logger.isWarnEnabled()) {
                logger.warn("There is no content for @NacosPropertySource from dataId : " + dataId + " , groupId : " + groupId);
            }
            return;
        }

        addPropertySource(name, config, nacosPropertySourceAttributes);

    }


    private void addPropertySource(String name, String config, Map<String, Object> nacosPropertySourceAttributes) {


        MutablePropertySources propertySources = environment.getPropertySources();

        Properties properties = toProperties(config);

        PropertiesPropertySource propertySource = new PropertiesPropertySource(name, properties);

        boolean isFirst = Boolean.TRUE.equals(nacosPropertySourceAttributes.get("first"));
        String before = (String) nacosPropertySourceAttributes.get("before");
        String after = (String) nacosPropertySourceAttributes.get("after");

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

    /**
     * The order is closed to {@link ConfigurationClassPostProcessor#getOrder() HIGHEST_PRECEDENCE} almost.
     *
     * @return <code>Ordered.HIGHEST_PRECEDENCE + 1</code>
     * @see ConfigurationClassPostProcessor#getOrder()
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
