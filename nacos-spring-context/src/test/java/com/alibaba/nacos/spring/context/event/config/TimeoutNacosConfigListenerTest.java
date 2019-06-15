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
package com.alibaba.nacos.spring.context.event.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.test.MockConfigService;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.nacos.spring.context.annotation.config.NacosPropertySourceTest.DATA_ID;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.GROUP_ID;

/**
 * {@link TimeoutNacosConfigListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class TimeoutNacosConfigListenerTest {

    private final ConfigService configService = new MockConfigService();

    private String receiveConfig(final long executionTime, long timeout, String content) throws NacosException {

        final AtomicReference<String> contentHolder = new AtomicReference<String>();

        Listener listener = new TimeoutNacosConfigListener(DATA_ID, GROUP_ID, timeout) {
            @Override
            protected void onReceived(String config) {
                doWait(executionTime);
                contentHolder.set(config);
                System.out.printf("[%s] %s \n", Thread.currentThread().getName(), config);
            }
        };

        configService.addListener(DATA_ID, GROUP_ID, listener);

        configService.publishConfig(DATA_ID, GROUP_ID, content);

        return contentHolder.get();
    }


    @Test
    public void test() throws NacosException {

        String content = "Hello,World";

        String receivedConfig = receiveConfig(20, 50, content);

        Assert.assertEquals(content, receivedConfig);
    }

    @Test
    public void testOnTimeout() throws NacosException {

        String content = "Hello,World";

        String receivedConfig = receiveConfig(100, 50, content);

        Assert.assertNull(receivedConfig);

    }

    private static void doWait(long millis) {

        long startTime = System.currentTimeMillis();

        while (true) {
            long costTime = System.currentTimeMillis() - startTime;
            if (costTime > millis) {
                break;
            }

        }
    }


}
