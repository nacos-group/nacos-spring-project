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

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.annotation.NacosConfigListener;
import com.alibaba.nacos.spring.context.annotation.NacosConfigListenerMethodProcessor;
import com.alibaba.nacos.spring.context.annotation.NacosService;
import com.alibaba.nacos.spring.mock.MockConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.mock.MockNacosServiceFactory.DATA_ID;

/**
 * {@link NacosConfigListenerMethodProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MockConfiguration.class,
        NamingServiceInjectedBeanPostProcessor.class,
        NacosConfigListenerMethodProcessor.class,
        NacosConfigListenerMethodProcessorTest.class,
        NacosConfigListenerMethodProcessorTest.ListenersConfiguration.class
})
public class NacosConfigListenerMethodProcessorTest {

    @NacosService
    private ConfigService configService;

    @Configuration
    public static class ListenersConfiguration {

        @NacosConfigListener(dataId = DATA_ID)
        public void onMessage(String value) {
            System.out.println("onMessage : " + value);
        }

        @NacosConfigListener(dataId = DATA_ID)
        public void onInteger(Integer value) {
            System.out.println("onInteger : " + value);
        }

        @NacosConfigListener(dataId = DATA_ID)
        public void onDouble(Double value) {
            System.out.println("onDouble : " + value);
        }

    }

    @Test
    public void testOn() throws NacosException {

        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");

    }

}
