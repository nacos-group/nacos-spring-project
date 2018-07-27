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

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.beans.factory.annotation.NamingServiceInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.context.annotation.NacosService;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.mock.MockConfiguration;
import com.alibaba.nacos.spring.mock.MockNacosConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.mock.MockNacosServiceFactory.DATA_ID;

/**
 * {@link NacosConfigPropertiesBindingPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MockConfiguration.class,
        NacosConfigPropertiesBindingPostProcessor.class,
        NamingServiceInjectedBeanPostProcessor.class,
        NacosConfigPropertiesBindingPostProcessorTest.class
})
public class NacosConfigPropertiesBindingPostProcessorTest {

    @Bean
    public MockNacosConfig config() {
        return new MockNacosConfig();
    }

    @Autowired
    private MockNacosConfig config;

    @NacosService
    private ConfigService configService;

    @Test
    public void test() throws NacosException {

        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "id=1\n name=mercyblitz\nvalue = 0.95");

        Assert.assertEquals(1, config.getId());
        Assert.assertEquals("mercyblitz", config.getName());
        Assert.assertTrue(0.95 == config.getValue());

        // Publishing config emits change
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "id=1\n name=mercyblitz@gmail.com\nvalue = 9527");

        Assert.assertEquals(1, config.getId());
        Assert.assertEquals("mercyblitz@gmail.com", config.getName());
        Assert.assertTrue(9527 == config.getValue());


    }

}
