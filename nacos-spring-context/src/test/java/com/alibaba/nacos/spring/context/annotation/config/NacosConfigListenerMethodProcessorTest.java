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
package com.alibaba.nacos.spring.context.annotation.config;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.beans.factory.annotation.AnnotationNacosInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.convert.converter.config.UserNacosConfigConverter;
import com.alibaba.nacos.spring.factory.ApplicationContextHolder;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.Listeners;
import com.alibaba.nacos.spring.test.TestConfiguration;
import com.alibaba.nacos.spring.test.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.annotation.PostConstruct;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link NacosConfigListenerMethodProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        TestConfiguration.class,
        Listeners.class,
        ConfigServiceBeanBuilder.class,
        AnnotationNacosInjectedBeanPostProcessor.class,
        NacosConfigListenerMethodProcessor.class,
        NacosConfigListenerMethodProcessorTest.class,
})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, NacosConfigListenerMethodProcessorTest.class})
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
public class NacosConfigListenerMethodProcessorTest extends AbstractNacosHttpServerTestExecutionListener {

    @Bean(name = ApplicationContextHolder.BEAN_NAME)
    public ApplicationContextHolder applicationContextHolder(ApplicationContext applicationContext) {
        ApplicationContextHolder applicationContextHolder = new ApplicationContextHolder();
        applicationContextHolder.setApplicationContext(applicationContext);
        return applicationContextHolder;
    }

    @Override
    protected String getServerAddressPropertyName() {
        return "server.addr";
    }

    @Autowired
    private Listeners listeners;

    @NacosInjected
    private ConfigService configService;

    @PostConstruct
    public void initListener() throws NacosException {
        configService.addListener(DATA_ID, DEFAULT_GROUP, new AbstractListener() {
            @Override
            public void receiveConfigInfo(String config) {
                assertEquals("9527", config); // asserts true
            }
        });
    }

    @NacosConfigListener(dataId = DATA_ID)
    public void onMessage(String config) {
        assertEquals("9527", config); // asserts true
    }

    @NacosConfigListener(dataId = DATA_ID)
    public void onInteger(Integer value) {
        assertEquals(Integer.valueOf(9527), value); // asserts true
    }

    @NacosConfigListener(dataId = DATA_ID)
    public void onInt(int value) {
        assertEquals(9527, value); // asserts true
    }

    @Test
    public void testPublishConfig() throws NacosException, InterruptedException {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");

        Thread.sleep(3000);

        assertNull(listeners.getIntegerValue()); // asserts true
        assertEquals(Double.valueOf(9527), listeners.getDoubleValue());   // asserts true
    }


    @Test
    public void testPublishUser() throws NacosException {
        configService.publishConfig("user", DEFAULT_GROUP, "{\"id\":1,\"name\":\"mercyblitz\"}");
    }

    @NacosConfigListener(dataId = "user", converter = UserNacosConfigConverter.class)
    public void onUser(User user) {
        assertEquals(Long.valueOf(1L), user.getId());
        assertEquals("mercyblitz", user.getName());
    }

}
