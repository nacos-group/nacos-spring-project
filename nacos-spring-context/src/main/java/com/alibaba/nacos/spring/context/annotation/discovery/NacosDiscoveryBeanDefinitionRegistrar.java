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
package com.alibaba.nacos.spring.context.annotation.discovery;

import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.util.NacosBeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotationMetadata;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.*;

/**
 * Nacos Discovery {@link ImportBeanDefinitionRegistrar BeanDefinition Registrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableNacosConfig
 * @see NacosBeanUtils#registerGlobalNacosProperties(AnnotationAttributes, BeanDefinitionRegistry, PropertyResolver, String)
 * @see NacosBeanUtils#registerNacosCommonBeans(BeanDefinitionRegistry)
 * @see NacosBeanUtils#registerNacosConfigBeans(BeanDefinitionRegistry, Environment)
 * @since 0.1.0
 */
public class NacosDiscoveryBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableNacosDiscovery.class.getName()));
        // Register Global Nacos Properties Bean
        registerGlobalNacosProperties(attributes, registry, environment, DISCOVERY_GLOBAL_NACOS_PROPERTIES_BEAN_NAME);
        // Register Nacos Common Beans
        registerNacosCommonBeans(registry);
        // Register Nacos Discovery Beans
        registerNacosDiscoveryBeans(registry);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
