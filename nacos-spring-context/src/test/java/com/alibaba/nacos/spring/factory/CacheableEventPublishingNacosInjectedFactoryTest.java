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
package com.alibaba.nacos.spring.factory;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

/**
 * {@link CacheableEventPublishingNacosServiceFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CacheableEventPublishingNacosServiceFactory.class})
public class CacheableEventPublishingNacosInjectedFactoryTest {

    @Autowired
    private NacosServiceFactory nacosServiceFactory;

    private Properties properties = new Properties();

    @Before
    public void init() {
        nacosServiceFactory = new CacheableEventPublishingNacosServiceFactory();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "127.0.0.1");
    }

    @Test
    public void testCreateConfigService() throws NacosException {

        ConfigService configService = nacosServiceFactory.createConfigService(properties);
        ConfigService configService2 = nacosServiceFactory.createConfigService(properties);

        Assert.assertTrue(configService == configService2);

    }

    @Test
    public void testCreateNamingService() throws NacosException {

        NamingService namingService = nacosServiceFactory.createNamingService(properties);
        NamingService namingService2 = nacosServiceFactory.createNamingService(properties);

        Assert.assertTrue(namingService == namingService2);
    }

}
