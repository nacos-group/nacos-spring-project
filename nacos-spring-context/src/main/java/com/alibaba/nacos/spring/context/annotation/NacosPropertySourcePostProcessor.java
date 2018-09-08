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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySources;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * {@link BeanFactoryPostProcessor Post Processor} resolves {@link NacosPropertySource @NacosPropertySource} or
 * {@link NacosPropertySources @NacosPropertySources} or {@link NacosPropertySourceBeanDefinition}
 * to be {@link PropertySource}, and append into Spring
 * {@link PropertySources}
 * {@link }
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @see NacosPropertySources
 * @see NacosPropertySourceBeanDefinition
 * @see PropertySource
 * @see BeanDefinitionRegistryPostProcessor
 * @since 0.1.0
 */
public class NacosPropertySourcePostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryPostProcessor,
        EnvironmentAware, Ordered, ApplicationEventPublisherAware {

    /**
     * The bean name of {@link NacosPropertySourcePostProcessor}
     */
    public static final String BEAN_NAME = "nacosPropertySourcePostProcessor";

    private ConfigurableEnvironment environment;

    private NacosPropertySourceProcessor processor;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinition annotationProcessor = BeanDefinitionBuilder.genericBeanDefinition(
                PropertySourcesPlaceholderConfigurer.class).getBeanDefinition();
        registry.registerBeanDefinition(PropertySourcesPlaceholderConfigurer.class.getName(), annotationProcessor);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        this.processor = new NacosPropertySourceProcessor(beanFactory, environment, applicationEventPublisher);

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
            addPropertySource(annotatedBeanDefinition);
        } else if (beanDefinition instanceof NacosPropertySourceBeanDefinition) {
            addPropertySource((NacosPropertySourceBeanDefinition) beanDefinition);
        }
    }

    private void addPropertySource(NacosPropertySourceBeanDefinition beanDefinition) {
        Map<String, Object> nacosPropertySourceAttributes = new HashMap<String, Object>();
        String[] attributeNames = beanDefinition.attributeNames();
        for (String attributeName : attributeNames) {
            Object attributeValue = beanDefinition.getAttribute(attributeName);
            nacosPropertySourceAttributes.put(attributeName, attributeValue);
        }
        addPropertySource(nacosPropertySourceAttributes);
    }

    private void addPropertySource(AnnotatedBeanDefinition annotatedBeanDefinition) {

        AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();

        Map<String, Object>[] annotationAttributesArray = resolveAnnotationAttributesArray(metadata);

        if (!isEmpty(annotationAttributesArray)) {
            for (Map<String, Object> annotationAttributes : annotationAttributesArray) {
                addPropertySource(annotationAttributes);
            }
        }
    }

    private Map<String, Object>[] resolveAnnotationAttributesArray(AnnotationMetadata metadata) {

        // Try to get @NacosPropertySources
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(NacosPropertySources.class.getName());

        final Map<String, Object>[] annotationAttributesArray;

        // If @NacosPropertySources annotated , get the attributes of @NacosPropertySource array from value() attribute
        if (annotationAttributes != null) {
            annotationAttributesArray = (Map<String, Object>[]) annotationAttributes.get("value");

        } else { // try to get @NacosPropertySource
            annotationAttributesArray = new Map[]{
                    metadata.getAnnotationAttributes(NacosPropertySource.class.getName())
            };
        }

        return annotationAttributesArray;
    }


    private void addPropertySource(Map<String, Object> nacosPropertySourceAttributes) {

        processor.process(nacosPropertySourceAttributes);

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

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
