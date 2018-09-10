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
package com.alibaba.nacos.spring.context.properties;

import com.alibaba.nacos.api.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.NacosBeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Properties;

import static com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource.CONFIG;
import static com.alibaba.nacos.spring.util.NacosUtils.resolveProperties;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * {@link NacosConfigurationProperties} Binding {@link BeanPostProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosConfigurationProperties
 * @see BeanPostProcessor
 * @since 0.1.0
 */
public class NacosConfigurationPropertiesBindingPostProcessor implements BeanPostProcessor, ApplicationContextAware,
        EnvironmentAware {

    /**
     * The name of {@link NacosConfigurationPropertiesBindingPostProcessor} Bean
     */
    public static final String BEAN_NAME = "nacosConfigurationPropertiesBindingPostProcessor";

    private Properties globalNacosProperties;

    private NacosServiceFactory nacosServiceFactory;

    private Environment environment;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        NacosConfigurationProperties nacosConfigurationProperties = findAnnotation(bean.getClass(), NacosConfigurationProperties.class);

        if (nacosConfigurationProperties != null) {
            bind(bean, beanName, nacosConfigurationProperties);
        }

        return bean;
    }

    private void bind(Object bean, String beanName, NacosConfigurationProperties nacosConfigurationProperties) {

        ConfigService configService = resolveConfigService(nacosConfigurationProperties);

        NacosConfigurationPropertiesBinder binder = new NacosConfigurationPropertiesBinder(configService);

        binder.bind(bean, nacosConfigurationProperties);

    }

    private ConfigService resolveConfigService(NacosConfigurationProperties nacosConfigurationProperties)
            throws BeansException {

        NacosProperties nacosProperties = nacosConfigurationProperties.properties();

        Properties properties = resolveProperties(nacosProperties, environment, globalNacosProperties);

        ConfigService configService = null;

        try {
            configService = nacosServiceFactory.createConfigService(properties);
        } catch (NacosException e) {
            throw new BeanCreationException(e.getErrMsg(), e);
        }

        return configService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        globalNacosProperties = CONFIG.getMergedGlobalProperties(applicationContext);
        nacosServiceFactory = NacosBeanUtils.getNacosServiceFactoryBean(applicationContext);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
