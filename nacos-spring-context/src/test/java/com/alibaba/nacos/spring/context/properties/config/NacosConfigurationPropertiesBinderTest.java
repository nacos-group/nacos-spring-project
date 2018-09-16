///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.nacos.spring.context.properties.config;
//
//import com.alibaba.nacos.api.config.ConfigService;
//import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
//import com.alibaba.nacos.api.exception.NacosException;
//import com.alibaba.nacos.spring.context.event.config.NacosConfigurationPropertiesBeanBoundEvent;
//import com.alibaba.nacos.spring.test.Config;
//import com.alibaba.nacos.spring.test.MockNacosServiceFactory;
//import org.junit.Assert;
//import org.junit.Test;
//import org.springframework.context.ApplicationEvent;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.event.ApplicationEventMulticaster;
//import org.springframework.context.event.SimpleApplicationEventMulticaster;
//
//import java.util.Properties;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
//import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.GROUP_ID;
//import static com.alibaba.nacos.spring.test.TestConfiguration.MODIFIED_TEST_CONTEXT;
//import static com.alibaba.nacos.spring.test.TestConfiguration.TEST_CONFIG;
//
///**
// * {@link NacosConfigurationPropertiesBinder} Test
// *
// * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
// * @since 0.1.0
// */
//public class NacosConfigurationPropertiesBinderTest {
//
//    private MockNacosServiceFactory nacosServiceFactory = new MockNacosServiceFactory();
//
//    @Test
//    public void testBind() throws NacosException {
//
//        final Config config = new Config();
//
//        final String beanName = "configBean";
//
//        ConfigService configService = nacosServiceFactory.createConfigService(new Properties());
//
//        final AtomicReference<String> content = new AtomicReference<String>();
//
//        content.set(TEST_CONFIG);
//
//        configService.publishConfig(DATA_ID, GROUP_ID, content.get());
//
//        final ApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
//
//        multicaster.addApplicationListener(new ApplicationListener<NacosConfigurationPropertiesBeanBoundEvent>() {
//            @Override
//            public void onApplicationEvent(NacosConfigurationPropertiesBeanBoundEvent event) {
//                Assert.assertEquals(DATA_ID, event.getDataId());
//                Assert.assertEquals(GROUP_ID, event.getGroupId());
//                Assert.assertEquals(content.get(), event.getContent());
//                Assert.assertEquals(config, event.getBean());
//                Assert.assertEquals(beanName, event.getBeanName());
//                Assert.assertEquals(config.getClass().getAnnotation(NacosConfigurationProperties.class), event.getProperties());
//            }
//        });
//
//        ApplicationEventPublisher applicationEventPublisher = new ApplicationEventPublisher() {
//
//            @Override
//            public void publishEvent(ApplicationEvent event) {
//                multicaster.multicastEvent(event);
//            }
//        };
//
//        NacosConfigurationPropertiesBinder binder = new NacosConfigurationPropertiesBinder(configService, applicationEventPublisher);
//
//        binder.bind(config, beanName);
//
//        Assert.assertEquals(1, config.getId());
//        Assert.assertEquals("mercyblitz", config.getName());
//        Assert.assertTrue(0.95 == config.getValue());
//        Assert.assertEquals(Float.valueOf(1234.5f), config.getFloatData());
//        Assert.assertNull(config.getIntData());
//
//        // Publishing config emits change
//        content.set(MODIFIED_TEST_CONTEXT);
//        configService.publishConfig(DATA_ID, GROUP_ID, content.get());
//
//        Assert.assertEquals(1, config.getId());
//        Assert.assertEquals("mercyblitz@gmail.com", config.getName());
//        Assert.assertTrue(9527 == config.getValue());
//        Assert.assertEquals(Float.valueOf(1234.5f), config.getFloatData());
//        Assert.assertNull(config.getIntData());
//    }
//
//}
