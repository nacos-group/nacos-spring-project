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
package com.alibaba.nacos.spring.context.annotation;

import com.alibaba.nacos.spring.test.EmbeddedNacosHttpServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.test.NacosConfigHttpHandler.CONTENT_PARAM_NAME;
import static com.alibaba.nacos.spring.test.NacosConfigHttpHandler.DATA_ID_PARAM_NAME;
import static com.alibaba.nacos.spring.test.NacosConfigHttpHandler.GROUP_ID_PARAM_NAME;

/**
 * {@link NacosPropertySource} {@link Value} Test
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1018</a>
 * @see NacosPropertySource
 * @see Value
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    NacosPropertySourceTest.class, NacosPropertySourceTest.AppNacosPropertySource.class
})
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${serverAddr}"))
@Component
public class NacosPropertySourceTest {

    private static final String DATA_ID = "app";

    private static final String APP_NAME = "Nacos-Spring";

    private static EmbeddedNacosHttpServer httpServer;

    static {
        System.setProperty("nacos.standalone", "true");
        try {
            httpServer = new EmbeddedNacosHttpServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.setProperty("serverAddr", "127.0.0.1:" + httpServer.getPort());
    }

    @BeforeClass
    public static void startServer() {
        initConfig();
        httpServer.start(true);
    }

    private static void initConfig() {
        Map<String, String> config = new HashMap<String, String>(1);
        config.put(DATA_ID_PARAM_NAME, DATA_ID);
        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
        config.put(CONTENT_PARAM_NAME, "app.name=" + APP_NAME);

        httpServer.initConfig(config);
    }

    @NacosPropertySource(dataId = DATA_ID)
    static class AppNacosPropertySource {

    }

    @Value("${app.name}")
    private String appName;

    @Test
    public void testValue() {
        Assert.assertEquals(APP_NAME, appName);
    }

}
