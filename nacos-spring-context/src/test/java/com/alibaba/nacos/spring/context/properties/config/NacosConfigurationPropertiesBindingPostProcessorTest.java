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
package com.alibaba.nacos.spring.context.properties.config;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.beans.factory.annotation.AnnotationNacosInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.Config;
import com.alibaba.nacos.spring.test.TestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.GROUP_ID;
import static com.alibaba.nacos.spring.test.TestConfiguration.MODIFIED_TEST_CONTEXT;
import static com.alibaba.nacos.spring.test.TestConfiguration.TEST_CONFIG;

/**
 * {@link NacosConfigurationPropertiesBindingPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        NacosConfigurationPropertiesBindingPostProcessorTest.class
})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, NacosConfigurationPropertiesBindingPostProcessorTest.class})
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
public class NacosConfigurationPropertiesBindingPostProcessorTest extends AbstractNacosHttpServerTestExecutionListener {

    @Autowired
    private Config config;

    @NacosInjected
    private ConfigService configService;

    @Bean
    public Config config() {
        return new Config();
    }

    @Test
    public void test() throws NacosException, InterruptedException {

        configService.publishConfig(DATA_ID, GROUP_ID, TEST_CONFIG);

        Thread.sleep(4000);

        Assert.assertEquals(1, config.getId());
        Assert.assertEquals("mercyblitz", config.getName());
        Assert.assertEquals(0.95, config.getValue(), 0.0);
        Assert.assertEquals(Float.valueOf(1234.5f), config.getFloatData());
        Assert.assertNull(config.getIntData());
        Assert.assertArrayEquals(new Object[]{1,2,3,4,5}, config.getList().toArray());
        Assert.assertEquals("value", config.getMap().get("key-1"));

        // Publishing config emits change
        configService.publishConfig(DATA_ID, GROUP_ID, MODIFIED_TEST_CONTEXT);

        Thread.sleep(2000);

        Assert.assertEquals(1, config.getId());
        Assert.assertEquals("mercyblitz@gmail.com", config.getName());
        Assert.assertEquals(9527, config.getValue(), 0.0);
        Assert.assertEquals(Float.valueOf(1234.5f), config.getFloatData());
        Assert.assertNull(config.getIntData());
        Assert.assertArrayEquals(new Object[]{6,6,6,6}, config.getList().toArray());
        Assert.assertNull(config.getMap().get("key-1"));
        Assert.assertEquals("value", config.getMap().get("key-2"));
        Assert.assertEquals("value", config.getMap().get("key-3"));
    }

    @Override
    protected String getServerAddressPropertyName() {
        return "server.addr";
    }

}
