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
package com.alibaba.nacos.spring.beans.factory.annotation;

import com.alibaba.nacos.api.annotation.NacosService;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.NacosBeanUtils;
import com.alibaba.spring.beans.factory.annotation.AnnotationInjectedBeanPostProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosUtils.resolveProperties;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * is used to inject {@link ConfigService} or {@link NamingService} instance into a Spring Bean If it's
 * attributes or properties annotated {@link NacosService @NacosService}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NamingServiceInjectedBeanPostProcessor extends AnnotationInjectedBeanPostProcessor<NacosService>
        implements InitializingBean {

    /**
     * The name of {@link NamingServiceInjectedBeanPostProcessor} bean
     */
    public static final String BEAN_NAME = "namingServiceInjectedBeanPostProcessor";

    private Properties globalNacosProperties;

    private NacosServiceFactory nacosServiceFactory;


    @Override
    public void afterPropertiesSet() throws Exception {
        // Get beanFactory from super
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();

        // Get Beans from ApplicationContext
        this.globalNacosProperties = NacosBeanUtils.getGlobalPropertiesBean(beanFactory);
        this.nacosServiceFactory = NacosBeanUtils.getNacosServiceFactoryBean(beanFactory);

    }

    @Override
    protected Object doGetInjectedBean(NacosService annotation, Object bean, String beanName, Class<?> injectedType,
                                       InjectionMetadata.InjectedElement injectedElement) throws Exception {
        Properties properties = resolveProperties(annotation.properties(), getEnvironment(), globalNacosProperties);

        if (ConfigService.class.equals(injectedType)) {
            return nacosServiceFactory.createConfigService(properties);
        } else if (NamingService.class.equals(injectedType)) {
            return nacosServiceFactory.createNamingService(properties);
        }

        throw new UnsupportedOperationException("Only support to inject ConfigService or NamingService instance, " +
                "actual class : " + injectedType.getName());
    }

    @Override
    protected String buildInjectedObjectCacheKey(NacosService annotation, Object bean, String beanName,
                                                 Class<?> injectedType, InjectionMetadata.InjectedElement injectedElement) {

        StringBuilder keyBuilder = new StringBuilder(injectedType.getName());

        Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);

        keyBuilder.append(annotation);

        return keyBuilder.toString();

    }
}
