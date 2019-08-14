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
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, NacosConfigListenerTest.class})
@ContextConfiguration(classes = {NacosConfigListenerTest.NacosConfiguration.class, NacosConfigListenerTest.class})
public class NacosConfigListenerTest extends AbstractNacosHttpServerTestExecutionListener {

    @Override
    protected String getServerAddressPropertyName() {
        return "server.addr";
    }

    @NacosInjected
    private ConfigService configService;

    private static volatile String content = "";
    private static volatile boolean receive = false;

    @NacosConfigListener(dataId = "com.alibaba.nacos.example.properties", timeout = 2000L)
    public void onMessage(String config) {
        System.out.println("onMessage: " + config);
        receive = true;
        content = config;
    }

    @Before
    public void before() {

    }

    @Test
    public void testConfigListener() throws InterruptedException {

        final long currentTimeMillis = System.currentTimeMillis();

        boolean result = false;
        try {
            result = configService.publishConfig("com.alibaba.nacos.example.properties", "DEFAULT_GROUP",
                    "" + currentTimeMillis);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(result);
        while (!receive) {
            TimeUnit.SECONDS.sleep(3);
        }
        Assert.assertEquals("" + currentTimeMillis, content);
    }

    @Configuration
    // 在命名空间详情处可以获取到 endpoint 和 namespace；accessKey 和 secretKey 推荐使用 RAM 账户的
    @EnableNacosConfig(globalProperties = @NacosProperties(
            serverAddr = "${server.addr}"
    ))
    public static class NacosConfiguration {

    }
}
