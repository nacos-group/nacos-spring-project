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

import com.alibaba.nacos.spring.context.annotation.NacosPropertiesResolver;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.spring.util.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

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
     * The bean name of {@link NacosPropertiesResolver}
     */
    public static final String NACOS_PROPERTIES_RESOLVER_BEAN_NAME = "nacosPropertiesResolver";

    /**
     * The bean name of {@link NacosConfigLoader}
     */
    public static final String NACOS_CONFIG_LOADER_BEAN_NAME = "nacosConfigLoader";

    /**
     * The bean name of {@link Executor} for Nacos Config Listener
     */
    public static final String NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME = "nacosConfigListenerExecutor";

    /**
     * Register an object to be Singleton Bean
     *
     * @param registry        {@link BeanDefinitionRegistry}
     * @param beanName        bean name
     * @param singletonObject singleton object
     */
    public static void registerSingleton(BeanDefinitionRegistry registry, String beanName, Object singletonObject) {
        SingletonBeanRegistry beanRegistry = null;
        if (registry instanceof SingletonBeanRegistry) {
            beanRegistry = (SingletonBeanRegistry) registry;
        } else if (registry instanceof AbstractApplicationContext) {
            // Maybe AbstractApplicationContext or its sub-classes
            beanRegistry = ((AbstractApplicationContext) registry).getBeanFactory();
        }
        // Register Singleton Object if possible
        if (beanRegistry != null) {
            beanRegistry.registerSingleton(beanName, singletonObject);
        }
    }

    /**
     * Is {@link BeanDefinition} present in {@link BeanDefinitionRegistry}
     *
     * @param registry        {@link BeanDefinitionRegistry}
     * @param beanName        the name of bean
     * @param targetBeanClass the type of bean
     * @return If Present , return <code>true</code>
     */
    public static boolean isBeanDefinitionPresent(BeanDefinitionRegistry registry, String beanName, Class<?> targetBeanClass) {
        String[] beanNames = BeanUtils.getBeanNames((ListableBeanFactory) registry, targetBeanClass);
        return ArrayUtils.contains(beanNames, beanName);
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
    public static NacosServiceFactory getNacosServiceFactoryBean(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(NACOS_SERVICE_FACTORY_BEAN_NAME, NacosServiceFactory.class);
    }

    /**
     * Get {@link NacosPropertiesResolver} Bean
     *
     * @param beanFactory {@link BeanFactory}
     * @return {@link NacosPropertiesResolver} Bean
     * @throws NoSuchBeanDefinitionException if there is no such bean definition
     */
    public static NacosPropertiesResolver getNacosPropertiesResolverBean(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(NACOS_PROPERTIES_RESOLVER_BEAN_NAME, NacosPropertiesResolver.class);
    }

    /**
     * Get {@link NacosConfigLoader} Bean
     *
     * @param beanFactory {@link BeanFactory}
     * @return {@link NacosConfigLoader} Bean
     * @throws NoSuchBeanDefinitionException if there is no such bean definition
     */
    public static NacosConfigLoader getNacosConfigLoaderBean(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(NACOS_CONFIG_LOADER_BEAN_NAME, NacosConfigLoader.class);
    }

    /**
     * Get {@link Executor} Bean for Nacos Config Listener
     *
     * @param beanFactory {@link BeanFactory}
     * @return {@link Executor} Bean
     * @throws NoSuchBeanDefinitionException if there is no such bean definition
     */
    public static ExecutorService getNacosConfigListenerExecutor(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME, ExecutorService.class);
    }
}
