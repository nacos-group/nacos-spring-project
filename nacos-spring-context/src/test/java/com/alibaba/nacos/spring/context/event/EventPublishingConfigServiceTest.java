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
package com.alibaba.nacos.spring.context.event;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.test.MockConfigService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

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

    @Before
    public void init() {
        this.mockConfigService = new MockConfigService();
        this.context = new GenericApplicationContext();
        this.context.refresh();
        this.configService = new EventPublishingConfigService(mockConfigService, context);
    }

    @Test
    public void testGetConfig() throws NacosException {
        mockConfigService.publishConfig(DATA_ID, GROUP_ID, CONTENT);
        Assert.assertEquals(CONTENT, mockConfigService.getConfig(DATA_ID, GROUP_ID, 5000));
    }

    @Test
    public void testPublishConfigWithEvent() throws NacosException {

        context.addApplicationListener(new ApplicationListener<NacosConfigPublishedEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigPublishedEvent event) {
                Assert.assertEquals(DATA_ID, event.getDataId());
                Assert.assertEquals(GROUP_ID, event.getGroupId());
                Assert.assertEquals(CONTENT, event.getContent());
                Assert.assertEquals(mockConfigService, event.getSource());
                Assert.assertTrue(event.isPublished());
            }
        });

        mockConfigService.publishConfig(DATA_ID, GROUP_ID, CONTENT);
    }

    @Test
    public void testRemoveConfigWithEvent() throws NacosException {

        context.addApplicationListener(new ApplicationListener<NacosConfigRemovedEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigRemovedEvent event) {
                Assert.assertEquals(DATA_ID, event.getDataId());
                Assert.assertEquals(GROUP_ID, event.getGroupId());
                Assert.assertEquals(mockConfigService, event.getSource());
                Assert.assertTrue(event.isRemoved());
            }
        });

        mockConfigService.publishConfig(DATA_ID, GROUP_ID, CONTENT);
        mockConfigService.removeConfig(DATA_ID, GROUP_ID);

    }
}


