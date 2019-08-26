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
import com.alibaba.nacos.spring.beans.factory.annotation.NamingMaintainServiceBeanBuilder;
import com.alibaba.nacos.spring.beans.factory.annotation.NamingServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.config.NacosConfigListenerMethodProcessor;
import com.alibaba.nacos.spring.context.annotation.config.NacosValueAnnotationBeanPostProcessor;
import com.alibaba.nacos.spring.context.event.LoggingNacosConfigMetadataEventListener;
import com.alibaba.nacos.spring.context.properties.config.NacosConfigurationPropertiesBindingPostProcessor;
import com.alibaba.nacos.spring.core.env.AnnotationNacosPropertySourceBuilder;
import com.alibaba.nacos.spring.core.env.NacosPropertySourcePostProcessor;
import com.alibaba.nacos.spring.core.env.XmlNacosPropertySourceBuilder;
import com.alibaba.nacos.spring.factory.ApplicationContextHolder;
import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.spring.util.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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
     * The bean name of global Nacos {@link Properties} for maintain
     */
    public static final String MAINTAIN_GLOBAL_NACOS_PROPERTIES_BEAN_NAME = GLOBAL_NACOS_PROPERTIES_BEAN_NAME +
            "$maintain";

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
            // Determine in advance whether injected with beans
            if (!beanRegistry.containsSingleton(beanName)) {
                beanRegistry.registerSingleton(beanName, singletonObject);
            }
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
        if (!isBeanDefinitionPresent(registry, beanName, beanClass) && !registry.containsBeanDefinition(beanName)) {
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

    public static void registerNacosApplicationContextHolder(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, ApplicationContextHolder.BEAN_NAME,
                ApplicationContextHolder.class);
    }

    public static void registerNacosConfigPropertiesBindingPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NacosConfigurationPropertiesBindingPostProcessor.BEAN_NAME,
                NacosConfigurationPropertiesBindingPostProcessor.class);
    }

    public static void registerNacosConfigListenerMethodProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NacosConfigListenerMethodProcessor.BEAN_NAME,
                NacosConfigListenerMethodProcessor.class);
    }

    public static void registerNacosPropertySourcePostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NacosPropertySourcePostProcessor.BEAN_NAME,
                NacosPropertySourcePostProcessor.class);
    }

    public static void registerAnnotationNacosPropertySourceBuilder(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, AnnotationNacosPropertySourceBuilder.BEAN_NAME,
                AnnotationNacosPropertySourceBuilder.class);
    }

    public static void registerXmlNacosPropertySourceBuilder(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, XmlNacosPropertySourceBuilder.BEAN_NAME,
                XmlNacosPropertySourceBuilder.class);
    }

    public static void registerNacosConfigListenerExecutor(BeanDefinitionRegistry registry, Environment environment) {
        final String beanName = NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME;
        if (registry instanceof BeanFactory && ((BeanFactory) registry).containsBean(beanName)) {
            return;
        }
        ExecutorService nacosConfigListenerExecutor = buildNacosConfigListenerExecutor(environment);
        registerSingleton(registry, beanName, nacosConfigListenerExecutor);
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
        // Register NacosApplicationContextHolder Bean
        registerNacosApplicationContextHolder(registry);
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

        registerNacosPropertySourcePostProcessor(registry);

        registerAnnotationNacosPropertySourceBuilder(registry);

        registerNacosConfigListenerExecutor(registry, environment);

        registerNacosValueAnnotationBeanPostProcessor(registry);

        registerConfigServiceBeanBuilder(registry);

        registerLoggingNacosConfigMetadataEventListener(registry);
    }

    /**
     * Invokes {@link NacosPropertySourcePostProcessor}
     *
     * @param beanFactory {@link BeanFactory}
     */
    public static void invokeNacosPropertySourcePostProcessor(BeanFactory beanFactory) {
        NacosPropertySourcePostProcessor postProcessor =
                beanFactory.getBean(NacosPropertySourcePostProcessor.BEAN_NAME, NacosPropertySourcePostProcessor.class);
        postProcessor.postProcessBeanFactory((ConfigurableListableBeanFactory) beanFactory);
    }

    /**
     * Register {@link LoggingNacosConfigMetadataEventListener} Bean
     *
     * @param registry {@link BeanDefinitionRegistry}
     */
    private static void registerLoggingNacosConfigMetadataEventListener(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, LoggingNacosConfigMetadataEventListener.BEAN_NAME,
                LoggingNacosConfigMetadataEventListener.class);
    }

    /**
     * Register Nacos Discovery Beans
     *
     * @param registry {@link BeanDefinitionRegistry}
     */
    public static void registerNacosDiscoveryBeans(BeanDefinitionRegistry registry) {
        registerNamingServiceBeanBuilder(registry);
        registerNamingMaintainServiceBeanBuilder(registry);
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

    private static void registerNamingMaintainServiceBeanBuilder(BeanDefinitionRegistry registry) {
        registerInfrastructureBeanIfAbsent(registry, NamingMaintainServiceBeanBuilder.BEAN_NAME, NamingMaintainServiceBeanBuilder.class);
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
        if (null == beanFactory) {
            return getNacosServiceFactoryBean();
        }
        ApplicationContextHolder applicationContextHolder = getApplicationContextHolder(beanFactory);
        CacheableEventPublishingNacosServiceFactory nacosServiceFactory = CacheableEventPublishingNacosServiceFactory.getSingleton();
        nacosServiceFactory.setApplicationContext(applicationContextHolder.getApplicationContext());
        return nacosServiceFactory;
    }

    public static NacosServiceFactory getNacosServiceFactoryBean() throws NoSuchBeanDefinitionException {
        return CacheableEventPublishingNacosServiceFactory.getSingleton();
    }

    public static ApplicationContextHolder getApplicationContextHolder(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(ApplicationContextHolder.BEAN_NAME, ApplicationContextHolder.class);
    }

    /**
     * Get {@link Executor} Bean for Nacos Config Listener If Present
     *
     * @param beanFactory {@link BeanFactory}
     * @return {@link Executor} Bean If Present, or <code>null</code>
     */
    public static ExecutorService getNacosConfigListenerExecutorIfPresent(BeanFactory beanFactory) {
        if (!beanFactory.containsBean(NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME)) {
            return null;
        }
        return beanFactory.getBean(NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME, ExecutorService.class);
    }

    /**
     * Get {@link ConfigServiceBeanBuilder} Bean
     *
     * @param beanFactory {@link BeanFactory}
     * @return {@link ConfigServiceBeanBuilder} Bean
     * @throws NoSuchBeanDefinitionException if there is no such bean definition
     */
    public static ConfigServiceBeanBuilder getConfigServiceBeanBuilder(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(ConfigServiceBeanBuilder.BEAN_NAME, ConfigServiceBeanBuilder.class);
    }

    /**
     * Get {@link NamingServiceBeanBuilder} Bean
     *
     * @param beanFactory {@link BeanFactory}
     * @return {@link NamingServiceBeanBuilder} Bean
     * @throws NoSuchBeanDefinitionException if there is no such bean definition
     */
    public static NamingServiceBeanBuilder getNamingServiceBeanBuilder(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(NamingServiceBeanBuilder.BEAN_NAME, NamingServiceBeanBuilder.class);
    }

    /**
     * Get {@link NamingMaintainServiceBeanBuilder} Bean
     *
     * @param beanFactory {@link BeanFactory}
     * @return {@link NamingMaintainServiceBeanBuilder} Bean
     * @throws NoSuchBeanDefinitionException if there is no such bean definition
     */
    public static NamingMaintainServiceBeanBuilder getNamingMaintainServiceBeanBuilder(BeanFactory beanFactory) throws NoSuchBeanDefinitionException {
        return beanFactory.getBean(NamingMaintainServiceBeanBuilder.BEAN_NAME, NamingMaintainServiceBeanBuilder.class);
    }

}
