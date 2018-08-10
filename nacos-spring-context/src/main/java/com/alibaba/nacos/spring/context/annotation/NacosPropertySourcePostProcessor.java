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
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getGlobalPropertiesBean;

/**
 * {@link NacosPropertySource @NacosPropertySource} Post Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySourcePostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryPostProcessor,
        EnvironmentAware, Ordered {

    /**
     * The bean name of {@link NacosPropertySourcePostProcessor}
     */
    public static final String BEAN_NAME = "nacosPropertySourcePostProcessor";

    private ConfigurableEnvironment environment;

    private NacosPropertySourceProcessor processor;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinition annotationProcessor = BeanDefinitionBuilder.genericBeanDefinition(
                PropertySourcesPlaceholderConfigurer.class).getBeanDefinition();
        registry.registerBeanDefinition(PropertySourcesPlaceholderConfigurer.class.getName(), annotationProcessor);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        this.processor = new NacosPropertySourceProcessor(beanFactory, environment);

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
}
