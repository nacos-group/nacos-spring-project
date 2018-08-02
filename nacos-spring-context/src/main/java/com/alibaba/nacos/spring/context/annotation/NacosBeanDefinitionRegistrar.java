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
import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.NacosConfigLoader;
import com.alibaba.nacos.spring.util.PropertiesPlaceholderResolver;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.nacos.spring.context.constants.NacosConstants.DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM;
import static com.alibaba.nacos.spring.context.constants.NacosConstants.NACOS_CONFIG_LISTENER_PARALLELISM;
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

        registerNacosPropertiesResolver(registry);

        registerNacosConfigLoader(registry);

        registerNamingServiceInjectedBeanPostProcessor(registry);

        registerNacosConfigPropertiesBindingPostProcessor(registry);

        registerNacosConfigListenerMethodProcessor(registry);

        registerNacosPropertySourceProcessor(registry);

        registerNacosConfigListenerExecutor(registry);

    }

    private void registerGlobalNacosProperties(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        AnnotationAttributes globalPropertiesAttributes = attributes.getAnnotation("globalProperties");
        // Resolve Global Nacos Properties from @EnableNacos
        PropertiesPlaceholderResolver resolver = new PropertiesPlaceholderResolver(environment);
        Properties globalProperties = resolver.resolve(globalPropertiesAttributes);
        // Register globalProperties Bean
        if (registry instanceof SingletonBeanRegistry) {
            SingletonBeanRegistry beanRegistry = (SingletonBeanRegistry) registry;
            beanRegistry.registerSingleton(GLOBAL_NACOS_PROPERTIES_BEAN_NAME, globalProperties);
        }
    }

    private void registerNacosServiceFactoryIfAbsent(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        String beanName = NACOS_SERVICE_FACTORY_BEAN_NAME;
        if (isBeanDefinitionPresent(registry, beanName, NacosServiceFactory.class)) {
            return;
        }
        registerInfrastructureBean(registry, beanName, CacheableEventPublishingNacosServiceFactory.class);
    }

    private void registerNacosPropertiesResolver(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, NACOS_PROPERTIES_RESOLVER_BEAN_NAME, NacosPropertiesResolver.class);
    }

    private void registerNacosConfigLoader(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, NACOS_CONFIG_LOADER_BEAN_NAME, NacosConfigLoader.class);
    }

    private void registerNamingServiceInjectedBeanPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, NamingServiceInjectedBeanPostProcessor.BEAN_NAME,
                NamingServiceInjectedBeanPostProcessor.class);
    }

    private void registerNacosConfigPropertiesBindingPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, NacosConfigPropertiesBindingPostProcessor.BEAN_NAME,
                NacosConfigPropertiesBindingPostProcessor.class);
    }

    private void registerNacosConfigListenerMethodProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, NacosConfigListenerMethodProcessor.BEAN_NAME,
                NacosConfigListenerMethodProcessor.class);
    }

    private void registerNacosPropertySourceProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, NacosPropertySourceProcessor.BEAN_NAME,
                NacosPropertySourceProcessor.class);
    }

    private void registerNacosConfigListenerExecutor(BeanDefinitionRegistry registry) {
        // Register globalProperties Bean
        if (registry instanceof SingletonBeanRegistry) {
            SingletonBeanRegistry beanRegistry = (SingletonBeanRegistry) registry;
            ExecutorService nacosConfigListenerExecutor = buildNacosConfigListenerExecutor();
            beanRegistry.registerSingleton(NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME, nacosConfigListenerExecutor);
        }
    }

    private ExecutorService buildNacosConfigListenerExecutor() {
        int parallelism = getParallelism();
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("NacosConfigListener-ThreadPool-" + threadNumber.getAndIncrement());
                return thread;
            }
        });
        return executorService;
    }

    private int getParallelism() {
        int parallelism = environment.getProperty(NACOS_CONFIG_LISTENER_PARALLELISM, int.class,
                DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM);
        return parallelism < 1 ? DEFAULT_NACOS_CONFIG_LISTENER_PARALLELISM : parallelism;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
