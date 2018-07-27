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

import com.alibaba.nacos.spring.beans.factory.annotation.NamingServiceInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.context.properties.NacosConfigPropertiesBindingPostProcessor;
import com.alibaba.nacos.spring.factory.CacheableNacosServiceFactory;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Properties;

import static com.alibaba.nacos.spring.beans.factory.annotation.NamingServiceInjectedBeanPostProcessor.BEAN_NAME;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.*;

/**
 * Nacos Properties {@link ImportBeanDefinitionRegistrar BeanDefinition Registrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableNacos
 * @since 0.1.0
 */
public class NacosBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableNacos.class.getName()));

        registerGlobalNacosProperties(attributes, registry);

        registerNacosServiceFactoryIfAbsent(attributes, registry);

        registerNamingServiceInjectedBeanPostProcessor(registry);

        registerNacosConfigPropertiesBindingPostProcessor(registry);

    }

    private void registerGlobalNacosProperties(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        AnnotationAttributes globalPropertiesAttributes = attributes.getAnnotation("globalProperties");
        // Resolve Global Nacos Properties from @EnableNacos
        Properties globalProperties = NacosUtils.resolveProperties(globalPropertiesAttributes, environment);
        registerInfrastructureBean(registry, GLOBAL_NACOS_PROPERTIES_BEAN_NAME, CacheableNacosServiceFactory.class, globalProperties);
    }

    private void registerNacosServiceFactoryIfAbsent(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        String beanName = NACOS_SERVICE_FACTORY_BEAN_NAME;
        if (isBeanDefinitionPresent(registry, beanName, NacosServiceFactory.class)) {
            return;
        }
        registerInfrastructureBean(registry, beanName, CacheableNacosServiceFactory.class);
    }

    private void registerNamingServiceInjectedBeanPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, NamingServiceInjectedBeanPostProcessor.BEAN_NAME,
                NamingServiceInjectedBeanPostProcessor.class);
    }

    private void registerNacosConfigPropertiesBindingPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, NacosConfigPropertiesBindingPostProcessor.BEAN_NAME,
                NacosConfigPropertiesBindingPostProcessor.class);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
