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
package com.alibaba.nacos.spring.core.env;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import com.alibaba.nacos.spring.context.config.xml.NacosPropertySourceXmlBeanDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySources;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getConfigServiceBeanBuilder;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_STRING_ATTRIBUTE_VALUE;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * {@link BeanFactoryPostProcessor Post Processor} resolves {@link com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource @NacosPropertySource} or
 * {@link NacosPropertySources @NacosPropertySources} or {@link NacosPropertySourceXmlBeanDefinition}
 * to be {@link PropertySource}, and append into Spring
 * {@link PropertySources}
 * {@link }
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource
 * @see NacosPropertySources
 * @see NacosPropertySourceXmlBeanDefinition
 * @see PropertySource
 * @see BeanDefinitionRegistryPostProcessor
 * @since 0.1.0
 */
public class NacosPropertySourcePostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryPostProcessor,
        EnvironmentAware, Ordered {

    /**
     * The bean name of {@link NacosPropertySourcePostProcessor}
     */
    public static final String BEAN_NAME = "nacosPropertySourcePostProcessor";

    private ConfigurableEnvironment environment;

    private Collection<AbstractNacosPropertySourceBuilder> nacosPropertySourceBuilders;

    private ConfigServiceBeanBuilder configServiceBeanBuilder;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        this.nacosPropertySourceBuilders = beanFactory.getBeansOfType(AbstractNacosPropertySourceBuilder.class).values();
        this.configServiceBeanBuilder = getConfigServiceBeanBuilder(beanFactory);

        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            processPropertySource(beanName, beanFactory);
        }

    }

    private void processPropertySource(String beanName, ConfigurableListableBeanFactory beanFactory) {

        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        // Build multiple instance if possible
        List<NacosPropertySource> nacosPropertySources = buildNacosPropertySources(beanName, beanDefinition);

        // Add Orderly
        for (NacosPropertySource nacosPropertySource : nacosPropertySources) {
            addNacosPropertySource(nacosPropertySource);
            addListenerIfAutoRefreshed(nacosPropertySource);
        }

    }

    private List<NacosPropertySource> buildNacosPropertySources(String beanName, BeanDefinition beanDefinition) {
        for (AbstractNacosPropertySourceBuilder builder : nacosPropertySourceBuilders) {
            if (builder.supports(beanDefinition)) {
                return builder.build(beanName, beanDefinition);
            }
        }
        return Collections.emptyList();
    }

    private void addNacosPropertySource(NacosPropertySource nacosPropertySource) {

        MutablePropertySources propertySources = environment.getPropertySources();

        boolean first = nacosPropertySource.isFirst();
        String before = nacosPropertySource.getBefore();
        String after = nacosPropertySource.getAfter();

        boolean hasBefore = !nullSafeEquals(DEFAULT_STRING_ATTRIBUTE_VALUE, before);
        boolean hasAfter = !nullSafeEquals(DEFAULT_STRING_ATTRIBUTE_VALUE, after);

        boolean isRelative = hasBefore || hasAfter;

        if (first) { // If First
            propertySources.addFirst(nacosPropertySource);
        } else if (isRelative) { // If relative
            if (hasBefore) {
                propertySources.addBefore(before, nacosPropertySource);
            }
            if (hasAfter) {
                propertySources.addAfter(after, nacosPropertySource);
            }
        } else {
            propertySources.addLast(nacosPropertySource); // default add last
        }
    }

    private void addListenerIfAutoRefreshed(final NacosPropertySource nacosPropertySource) {

        if (!nacosPropertySource.isAutoRefreshed()) { // Disable Auto-Refreshed
            return;
        }

        final String dataId = nacosPropertySource.getDataId();
        final String groupId = nacosPropertySource.getGroupId();
        final Map<String, Object> nacosPropertiesAttributes = nacosPropertySource.getAttributesMetadata();
        final ConfigService configService = configServiceBeanBuilder.build(nacosPropertiesAttributes);

        try {
            configService.addListener(dataId, groupId, new AbstractListener() {

                @Override
                public void receiveConfigInfo(String config) {
                    String name = nacosPropertySource.getName();
                    NacosPropertySource newNacosPropertySource = new NacosPropertySource(name, config);
                    newNacosPropertySource.copy(nacosPropertySource);
                    MutablePropertySources propertySources = environment.getPropertySources();
                    // replace NacosPropertySource
                    propertySources.replace(name, newNacosPropertySource);
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException("ConfigService can't add Listener with properties : " + nacosPropertiesAttributes, e);
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
