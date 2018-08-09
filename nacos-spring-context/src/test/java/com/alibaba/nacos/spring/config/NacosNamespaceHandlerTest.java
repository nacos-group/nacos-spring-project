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
package com.alibaba.nacos.spring.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.spring.context.annotation.GlobalNacosPropertiesBeanDefinitionParser;
import com.alibaba.nacos.spring.context.annotation.NacosAnnotationDrivenBeanDefinitionParser;
import com.alibaba.nacos.spring.context.annotation.NacosService;
import com.alibaba.nacos.spring.test.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.test.User;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
import static com.alibaba.nacos.spring.test.NacosConfigHttpHandler.CONTENT_PARAM_NAME;
import static com.alibaba.nacos.spring.test.NacosConfigHttpHandler.DATA_ID_PARAM_NAME;
import static com.alibaba.nacos.spring.test.NacosConfigHttpHandler.GROUP_ID_PARAM_NAME;

/**
 * {@link NacosNamespaceHandler} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see GlobalNacosPropertiesBeanDefinitionParser
 * @see NacosAnnotationDrivenBeanDefinitionParser
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/nacos-context.xml"
})
public class NacosNamespaceHandlerTest {

    private static final Long USER_ID = 1991L;
    private static final String USER_NAME = "hxy";

    private static EmbeddedNacosHttpServer httpServer;

    static {
        System.setProperty("nacos.standalone", "true");
        try {
            httpServer = new EmbeddedNacosHttpServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.setProperty("nacos.server-addr", "127.0.0.1:" + httpServer.getPort());
    }

    @BeforeClass
    public static void startServer() {
        initConfig();
        httpServer.start(true);
    }

    private static void initConfig() {
        Map<String, String> config = new HashMap<String, String>(1);
        config.put(DATA_ID_PARAM_NAME, "user");
        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
        config.put(CONTENT_PARAM_NAME, "id=" + USER_ID + "\nname=" + USER_NAME);

        httpServer.initConfig(config);
    }

    @AfterClass
    public static void stopServer() {
        httpServer.stop();
    }

    @NacosService
    private ConfigService configService;

    @Autowired
    private User user;

    @Test
    public void testGetConfig() throws Exception {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
        Assert.assertEquals("9527", configService.getConfig(DATA_ID, DEFAULT_GROUP, 5000));

        Assert.assertEquals(USER_ID, user.getId());
        Assert.assertEquals(USER_NAME, user.getName());
    }

}
