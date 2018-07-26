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
package com.alibaba.nacos.spring.util;

import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * Nacos Bean Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public abstract class NacosBeanUtils {

    /**
     * The bean name of global Nacos {@link Properties}
     */
    public static final String GLOBAL_NACOS_PROPERTIES_BEAN_NAME = "globalNacosProperties";

    /**
     * The bean name of {@link NacosServiceFactory}
     */
    public static final String NACOS_SERVICE_FACTORY_BEAN_NAME = "nacosServiceFactory";

    /**
     * Is {@link BeanDefinition} present in {@link BeanDefinitionRegistry}
     *
     * @param registry  {@link BeanDefinitionRegistry}
     * @param beanName  the name of bean
     * @param beanClass the type of bean
     * @return If Present , return <code>true</code>
     */
    public static boolean isBeanDefinitionPresent(BeanDefinitionRegistry registry, String beanName, Class<?> beanClass) {

        boolean present = false;

        if (registry.containsBeanDefinition(beanName)) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            present = beanClass.getName().equals(beanClassName);
        }

        return present;

    }

    /**
     * Register Infrastructure Bean
     *
     * @param registry        {@link BeanDefinitionRegistry}
     * @param beanName        the name of bean
     * @param beanClass       the class of bean
     * @param constructorArgs the arguments of {@link Constructor}
     */
    public static void registerInfrastructureBean(BeanDefinitionRegistry registry, String beanName, Class<?> beanClass,
                                                  Object... constructorArgs) {
        // Build a BeanDefinition for NacosServiceFactory class
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        for (Object constructorArg : constructorArgs) {
            beanDefinitionBuilder.addConstructorArgValue(constructorArg);
        }
        // ROLE_INFRASTRUCTURE
        beanDefinitionBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        // Register
        registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }

    /**
     * Get Global Properties Bean
     *
     * @param beanFactory {@link BeanFactory}
     * @return Global Properties Bean
     * @throws NoSuchBeanDefinitionException if there is no such bean definition
     */
    public static Properties getGlobalPropertiesBean(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(GLOBAL_NACOS_PROPERTIES_BEAN_NAME, Properties.class);
    }

    /**
     * Get {@link NacosServiceFactory} Bean
     *
     * @param beanFactory {@link BeanFactory}
     * @return {@link NacosServiceFactory} Bean
     * @throws NoSuchBeanDefinitionException if there is no such bean definition
     */
    public static NacosServiceFactory getNacosServiceFactory(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(NACOS_SERVICE_FACTORY_BEAN_NAME, NacosServiceFactory.class);
    }
}
