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
import com.alibaba.nacos.spring.test.Config;
import com.alibaba.nacos.spring.test.MockNacosServiceFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.GROUP_ID;
import static com.alibaba.nacos.spring.test.TestConfiguration.MODIFIED_TEST_CONTEXT;
import static com.alibaba.nacos.spring.test.TestConfiguration.TEST_CONFIG;

/**
 * {@link NacosConfigurationPropertiesBinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigurationPropertiesBinderTest {

    private MockNacosServiceFactory nacosServiceFactory = new MockNacosServiceFactory();

    @Test
    public void testBind() throws NacosException {

        Config config = new Config();

        ConfigService configService = nacosServiceFactory.createConfigService(new Properties());

        configService.publishConfig(DATA_ID, GROUP_ID, TEST_CONFIG);

        NacosConfigurationPropertiesBinder binder = new NacosConfigurationPropertiesBinder(configService);

        binder.bind(config);

        Assert.assertEquals(1, config.getId());
        Assert.assertEquals("mercyblitz", config.getName());
        Assert.assertTrue(0.95 == config.getValue());
        Assert.assertEquals(Float.valueOf(1234.5f), config.getFloatData());
        Assert.assertNull(config.getIntData());

        // Publishing config emits change
        configService.publishConfig(DATA_ID, GROUP_ID, MODIFIED_TEST_CONTEXT);

        Assert.assertEquals(1, config.getId());
        Assert.assertEquals("mercyblitz@gmail.com", config.getName());
        Assert.assertTrue(9527 == config.getValue());
        Assert.assertEquals(Float.valueOf(1234.5f), config.getFloatData());
        Assert.assertNull(config.getIntData());
    }

}
