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

import com.alibaba.nacos.spring.beans.factory.annotation.AnnotationNacosInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.beans.factory.annotation.NamingServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.NacosConfigListenerMethodProcessor;
import com.alibaba.nacos.spring.context.annotation.NacosPropertySourcePostProcessor;
import com.alibaba.nacos.spring.context.annotation.NacosValueAnnotationBeanPostProcessor;
import com.alibaba.nacos.spring.context.properties.NacosConfigurationPropertiesBindingPostProcessor;
import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;
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
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.nacos.spring.context.constants.NacosConstants.DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM;
import static com.alibaba.nacos.spring.context.constants.NacosConstants.NACOS_CONFIG_LISTENER_PARALLELISM;
import static com.alibaba.nacos.spring.util.NacosUtils.resolveProperties;

/**
 * Nacos Bean Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public abstract class NacosBeanUtils {

    /**
     * The bean name of {@link PropertySourcesPlaceholderConfigurer}
     */
    public static final String PLACEHOLDER_CONFIGURER_BEAN_NAME = "propertySourcesPlaceholderConfigurer";

    /**
     * The bean name of global Nacos {@link Properties}
     */
    public static final String GLOBAL_NACOS_PROPERTIES_BEAN_NAME = "globalNacosProperties";

    /**
     * The bean name of global Nacos {@link Properties} for config
     */
    public static final String CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME = GLOBAL_NACOS_PROPERTIES_BEAN_NAME
            + "$config";

    /**
     * The bean name of global Nacos {@link Properties} for discovery
     */
    public static final String DISCOVERY_GLOBAL_NACOS_PROPERTIES_BEAN_NAME = GLOBAL_NACOS_PROPERTIES_BEAN_NAME +
            "$discovery";

    /**
     * The bean name of {@link NacosServiceFactory}
     */
    public static final String NACOS_SERVICE_FACTORY_BEAN_NAME = "nacosServiceFactory";

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
     * Register Infrastructure Bean if absent
     *
     * @param registry        {@link BeanDefinitionRegistry}
     * @param beanName        the name of bean
     * @param beanClass       the class of bean
     * @param constructorArgs the arguments of {@link Constructor}
     */
    public static void registerInfrastructureBeanIfAbsent(BeanDefinitionRegistry registry, String beanName, Class<?> beanClass,
                                                          Object... constructorArgs) {
        if (!isBeanDefinitionPresent(registry, beanName, beanClass)) {
            registerInfrastructureBean(registry, beanName, beanClass, constructorArgs);
        }
    }

    /**
     * Resolve {@link BeanFactory} from {@link BeanDefinitionRegistry}
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @return {@link BeanFactory} if resolved
     */
    public static BeanFactory resolveBeanFactory(BeanDefinitionRegistry registry) {
        if (registry instanceof BeanFactory) {
            return (BeanFactory) registry;
        }

        if (registry instanceof AbstractApplicationContext) {
            return ((AbstractApplicationContext) registry).getBeanFactory();
        }
        return null;
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
     * Register {@link PropertySourcesPlaceholderConfigurer} Bean
     *
     * @param registry {@link BeanDefinitionRegistry}
     */
    public static void registerPropertySourcesPlaceholderConfigurer(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, PLACEHOLDER_CONFIGURER_BEAN_NAME,
                PropertySourcesPlaceholderConfigurer.class);
    }


    /**
     * Register Global Nacos Properties Bean with specified name
     *
     * @param attributes       the attributes of Global Nacos Properties may contain placeholders
     * @param registry         {@link BeanDefinitionRegistry}
     * @param propertyResolver {@link PropertyResolver}
     * @param beanName         Bean name
     */
    public static void registerGlobalNacosProperties(AnnotationAttributes attributes,
                                                     BeanDefinitionRegistry registry,
                                                     PropertyResolver propertyResolver,
                                                     String beanName) {
        if (attributes == null) {
            return; // Compatible with null
        }
        AnnotationAttributes globalPropertiesAttributes = attributes.getAnnotation("globalProperties");
        registerGlobalNacosProperties((Map<?, ?>) globalPropertiesAttributes, registry, propertyResolver,
                beanName);
    }

    /**
     * Register Global Nacos Properties Bean with specified name
     *
     * @param globalPropertiesAttributes the attributes of Global Nacos Properties may contain placeholders
     * @param registry                   {@link BeanDefinitionRegistry}
     * @param propertyResolver           {@link PropertyResolver}
     * @param beanName                   Bean name
     */
    public static void registerGlobalNacosProperties(Map<?, ?> globalPropertiesAttributes,
                                                     BeanDefinitionRegistry registry,
                                                     PropertyResolver propertyResolver,
                                                     String beanName) {
        Properties globalProperties = resolveProperties(globalPropertiesAttributes, propertyResolver);
        registerSingleton(registry, beanName, globalProperties);
    }


    /**
     * Register {@link CacheableEventPublishingNacosServiceFactory NacosServiceFactory}
     *
     * @param registry {@link BeanDefinitionRegistry}
     */
    public static void registerNacosServiceFactory(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NACOS_SERVICE_FACTORY_BEAN_NAME, CacheableEventPublishingNacosServiceFactory.class);
    }

    public static void registerNacosConfigPropertiesBindingPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NacosConfigurationPropertiesBindingPostProcessor.BEAN_NAME,
                NacosConfigurationPropertiesBindingPostProcessor.class);
    }

    public static void registerNacosConfigListenerMethodProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NacosConfigListenerMethodProcessor.BEAN_NAME,
                NacosConfigListenerMethodProcessor.class);
    }

    public static void registerNacosPropertySourceProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NacosPropertySourcePostProcessor.BEAN_NAME,
                NacosPropertySourcePostProcessor.class);
    }

    public static void registerNacosConfigListenerExecutor(BeanDefinitionRegistry registry, Environment environment) {
        ExecutorService nacosConfigListenerExecutor = buildNacosConfigListenerExecutor(environment);
        registerSingleton(registry, NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME, nacosConfigListenerExecutor);
    }

    private static ExecutorService buildNacosConfigListenerExecutor(Environment environment) {
        int parallelism = getParallelism(environment);
        return Executors.newFixedThreadPool(parallelism, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("NacosConfigListener-ThreadPool-" + threadNumber.getAndIncrement());
                return thread;
            }
        });
    }

    private static int getParallelism(Environment environment) {
        int parallelism = environment.getProperty(NACOS_CONFIG_LISTENER_PARALLELISM, int.class,
                DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM);
        return parallelism < 1 ? DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM : parallelism;
    }

    public static void registerNacosValueAnnotationBeanPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NacosValueAnnotationBeanPostProcessor.BEAN_NAME,
                NacosValueAnnotationBeanPostProcessor.class);
    }

    /**
     * Register Nacos Common Beans
     *
     * @param registry {@link BeanDefinitionRegistry}
     */
    public static void registerNacosCommonBeans(BeanDefinitionRegistry registry) {
        // Register NacosServiceFactory Bean
        registerNacosServiceFactory(registry);
        // Register AnnotationNacosInjectedBeanPostProcessor Bean
        registerAnnotationNacosInjectedBeanPostProcessor(registry);
    }

    /**
     * Register Nacos Config Beans
     *
     * @param registry    {@link BeanDefinitionRegistry}
     * @param environment {@link Environment}
     */
    public static void registerNacosConfigBeans(BeanDefinitionRegistry registry, Environment environment) {
        // Register PropertySourcesPlaceholderConfigurer Bean
        registerPropertySourcesPlaceholderConfigurer(registry);

        registerNacosConfigPropertiesBindingPostProcessor(registry);

        registerNacosConfigListenerMethodProcessor(registry);

        registerNacosPropertySourceProcessor(registry);

        registerNacosConfigListenerExecutor(registry, environment);

        registerNacosValueAnnotationBeanPostProcessor(registry);

        registerConfigServiceBeanBuilder(registry);
    }

    /**
     * Register Nacos Discovery Beans
     *
     * @param registry {@link BeanDefinitionRegistry}
     */
    public static void registerNacosDiscoveryBeans(BeanDefinitionRegistry registry) {
        registerNamingServiceBeanBuilder(registry);
    }

    /**
     * Register {@link AnnotationNacosInjectedBeanPostProcessor} with
     * {@link AnnotationNacosInjectedBeanPostProcessor#BEAN_NAME name}
     *
     * @param registry {@link BeanDefinitionRegistry}
     */
    private static void registerAnnotationNacosInjectedBeanPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, AnnotationNacosInjectedBeanPostProcessor.BEAN_NAME,
                AnnotationNacosInjectedBeanPostProcessor.class);
    }

    private static void registerConfigServiceBeanBuilder(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, ConfigServiceBeanBuilder.BEAN_NAME, ConfigServiceBeanBuilder.class);
    }

    private static void registerNamingServiceBeanBuilder(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NamingServiceBeanBuilder.BEAN_NAME, NamingServiceBeanBuilder.class);
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
