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
//package com.alibaba.nacos.spring.beans.factory.annotation;
//
//import com.alibaba.nacos.api.config.ConfigService;
//import com.alibaba.nacos.spring.context.annotation.EnableNacos;
//import com.alibaba.nacos.spring.context.annotation.NacosBeanDefinitionRegistrar;
//import com.alibaba.nacos.spring.context.annotation.NacosProperties;
//import com.alibaba.nacos.spring.context.annotation.NacosService;
//import com.alibaba.nacos.spring.test.Config;
//import com.alibaba.nacos.spring.test.ListenersConfiguration;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
//import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
//
///**
// * {@link NacosBeanDefinitionRegistrar} Test
// *
// * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
// * @since 0.1.0
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {
//        ListenersConfiguration.class,
//        NacosBeanDefinitionRegistrarTest.class,
//})
//@EnableNacos(globalProperties = @NacosProperties(serverAddr = "11.163.128.36")) // Integration Test
//public class NacosBeanDefinitionRegistrarTest {
//
//    @NacosService
//    private ConfigService configService;
//
//    @Autowired
//    private Config config;
//
//    @Bean
//    public Config config() {
//        return new Config();
//    }
//
//    @Test
//    public void testPublish() throws Exception {
//
//        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
//        // Publish User
//        configService.publishConfig("user", DEFAULT_GROUP, "{\"id\":1,\"name\":\"mercyblitz\"}");
//
//        Thread.sleep(1000);
//    }
//
//
//    @Test
//    public void testBind() throws Exception {
//
//        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "id=1\n name=mercyblitz\nvalue = 0.95");
//
//        Thread.sleep(1000);
//
//        Assert.assertEquals(1, config.getId());
//        Assert.assertEquals("mercyblitz", config.getName());
//        Assert.assertTrue(0.95 == config.getValue());
//
//    }
//
//}
