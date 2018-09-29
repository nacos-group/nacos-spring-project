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
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.metadata.NacosServiceMetaData;
import com.alibaba.nacos.spring.test.MockConfigService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Properties;
import java.util.concurrent.Executor;

import static com.alibaba.nacos.spring.test.MockConfigService.TIMEOUT_ERROR_MESSAGE;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.*;

/**
 * {@link EventPublishingConfigService} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class EventPublishingConfigServiceTest {

    private ConfigService mockConfigService;

    private ConfigurableApplicationContext context;

    private ConfigService configService;

    private Properties properties = new Properties();

    @Before
    public void init() {
        this.mockConfigService = new MockConfigService();
        this.context = new GenericApplicationContext();
        this.context.refresh();
        this.configService = new EventPublishingConfigService(mockConfigService, properties, context, new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });
    }

    @After
    public void destroy() {
        this.context.close();
    }

    @Test
    public void testGetConfig() throws NacosException {
        configService.publishConfig(DATA_ID, GROUP_ID, CONTENT);
        Assert.assertEquals(CONTENT, configService.getConfig(DATA_ID, GROUP_ID, 5000));
    }

    @Test(expected = NacosException.class)
    public void testGetConfigOnTimeout() throws NacosException {
        final long timeout = -1L;
        context.addApplicationListener(new ApplicationListener<NacosConfigTimeoutEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigTimeoutEvent event) {
                assertNacosConfigEvent(event);
                Assert.assertEquals(timeout, event.getTimeout());
                Assert.assertEquals(TIMEOUT_ERROR_MESSAGE, event.getErrorMessage());
            }
        });

        configService.getConfig(DATA_ID, GROUP_ID, timeout); // trigger timeout error
    }

    @Test
    public void testPublishConfigWithEvent() throws NacosException {

        context.addApplicationListener(new ApplicationListener<NacosConfigPublishedEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigPublishedEvent event) {
                assertNacosConfigEvent(event);
                Assert.assertEquals(CONTENT, event.getContent());
                Assert.assertTrue(event.isPublished());
            }
        });

        configService.publishConfig(DATA_ID, GROUP_ID, CONTENT);
    }

    @Test
    public void testRemoveConfigWithEvent() throws NacosException {

        context.addApplicationListener(new ApplicationListener<NacosConfigRemovedEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigRemovedEvent event) {
                assertNacosConfigEvent(event);
                Assert.assertTrue(event.isRemoved());
            }
        });

        configService.publishConfig(DATA_ID, GROUP_ID, CONTENT);
        configService.removeConfig(DATA_ID, GROUP_ID);

    }

    @Test
    public void testAddListener() throws NacosException {

        final Listener listener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                Assert.assertEquals(CONTENT, configInfo);
            }
        };

        // assert NacosConfigReceivedEvent
        context.addApplicationListener(new ApplicationListener<NacosConfigReceivedEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigReceivedEvent event) {
                assertNacosConfigEvent(event);
                Assert.assertEquals(CONTENT, event.getContent());
            }
        });

        // assert NacosConfigListenerRegisteredEvent
        context.addApplicationListener(new ApplicationListener<NacosConfigListenerRegisteredEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigListenerRegisteredEvent event) {
                assertNacosConfigEvent(event);
                Assert.assertTrue(event.isRegistered());
                Assert.assertEquals(listener, event.getListener());
            }
        });

        // Add Listener
        configService.addListener(DATA_ID, GROUP_ID, listener);
        // Publish Config
        configService.publishConfig(DATA_ID, GROUP_ID, CONTENT);

    }

    @Test
    public void testRemoveListener() throws NacosException {
        final Listener listener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
            }
        };

        // assert NacosConfigListenerRegisteredEvent
        context.addApplicationListener(new ApplicationListener<NacosConfigListenerRegisteredEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigListenerRegisteredEvent event) {
                assertNacosConfigEvent(event);
                Assert.assertFalse(event.isRegistered());
                Assert.assertEquals(listener, event.getListener());
            }
        });

        configService.removeListener(DATA_ID, GROUP_ID, listener);

    }

    private void assertNacosConfigEvent(NacosConfigEvent event) {
        Assert.assertEquals(mockConfigService, event.getSource());
        Assert.assertEquals(DATA_ID, event.getDataId());
        Assert.assertEquals(GROUP_ID, event.getGroupId());
    }

    @Test
    public void testGetProperties() {
        Assert.assertSame(properties, ((NacosServiceMetaData) configService).getProperties());
    }

}


