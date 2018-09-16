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

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.beans.factory.annotation.AnnotationNacosInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.beans.factory.annotation.NamingServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosConfigListenerMethodProcessor;
import com.alibaba.nacos.spring.context.annotation.config.NacosValueAnnotationBeanPostProcessor;
import com.alibaba.nacos.spring.context.annotation.discovery.EnableNacosDiscovery;
import com.alibaba.nacos.spring.context.properties.config.NacosConfigurationPropertiesBindingPostProcessor;
import com.alibaba.nacos.spring.core.env.AnnotationNacosPropertySourceBuilder;
import com.alibaba.nacos.spring.core.env.NacosPropertySourcePostProcessor;
import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.Config;
import com.alibaba.nacos.spring.util.NacosBeanUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.PLACEHOLDER_CONFIGURER_BEAN_NAME;

/**
 * {@link NacosBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        NacosBeanDefinitionRegistrarTest.class,
})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, NacosBeanDefinitionRegistrarTest.class})
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${serverAddr}"))
@EnableNacosConfig
@EnableNacosDiscovery
public class NacosBeanDefinitionRegistrarTest extends AbstractNacosHttpServerTestExecutionListener {

    @Override
    protected String getServerAddressPropertyName() {
        return "serverAddr";
    }

    @Bean
    public Config config() {
        return new Config();
    }

    @Autowired
    @Qualifier(NacosBeanUtils.GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    private Properties globalProperties;

    @Autowired
    @Qualifier(NacosBeanUtils.CONFIG_GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    private Properties configGlobalProperties;
    @Autowired
    @Qualifier(NacosBeanUtils.DISCOVERY_GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    private Properties discoveryGlobalProperties;

    @Autowired
    @Qualifier(CacheableEventPublishingNacosServiceFactory.BEAN_NAME)
    private NacosServiceFactory nacosServiceFactory;

    @Autowired
    @Qualifier(AnnotationNacosInjectedBeanPostProcessor.BEAN_NAME)
    private AnnotationNacosInjectedBeanPostProcessor annotationNacosInjectedBeanPostProcessor;

    @Autowired
    @Qualifier(PLACEHOLDER_CONFIGURER_BEAN_NAME)
    private PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;

    @Autowired
    @Qualifier(NacosConfigurationPropertiesBindingPostProcessor.BEAN_NAME)
    private NacosConfigurationPropertiesBindingPostProcessor nacosConfigurationPropertiesBindingPostProcessor;

    @Autowired
    @Qualifier(NacosConfigListenerMethodProcessor.BEAN_NAME)
    private NacosConfigListenerMethodProcessor nacosConfigListenerMethodProcessor;

    @Autowired
    @Qualifier(NacosPropertySourcePostProcessor.BEAN_NAME)
    private NacosPropertySourcePostProcessor nacosPropertySourcePostProcessor;

    @Autowired
    @Qualifier(AnnotationNacosPropertySourceBuilder.BEAN_NAME)
    private AnnotationNacosPropertySourceBuilder annotationNacosPropertySourceBuilder;

    @Autowired
    @Qualifier(NacosValueAnnotationBeanPostProcessor.BEAN_NAME)
    private NacosValueAnnotationBeanPostProcessor nacosValueAnnotationBeanPostProcessor;

    @Autowired
    @Qualifier(ConfigServiceBeanBuilder.BEAN_NAME)
    private ConfigServiceBeanBuilder configServiceBeanBuilder;

    @Autowired
    @Qualifier(NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME)
    private ExecutorService nacosConfigListenerExecutor;

    @Autowired
    @Qualifier(NamingServiceBeanBuilder.BEAN_NAME)
    private NamingServiceBeanBuilder namingServiceBeanBuilder;

    @NacosInjected
    private ConfigService globalConfigService;

    @NacosInjected(properties = @NacosProperties(serverAddr = "${serverAddr}"))
    private ConfigService configService;

    @NacosInjected
    private NamingService namingService;

    @Autowired
    private Config config;

    @Value("${user.home:${user.dir}}")
    private String dir;

    @Test
    public void testGetConfig() throws Exception {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
        Assert.assertEquals("9527", configService.getConfig(DATA_ID, DEFAULT_GROUP, 5000));
    }


}
