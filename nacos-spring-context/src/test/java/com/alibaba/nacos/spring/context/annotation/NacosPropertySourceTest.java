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

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosValue;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.EmbeddedNacosHttpServer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.test.NacosConfigHttpHandler.*;

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
        NacosPropertySourceTest.class
})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, NacosPropertySourceTest.class})

@NacosPropertySource(dataId = NacosPropertySourceTest.DATA_ID, autoRefreshed = true)
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
@Component
public class NacosPropertySourceTest extends AbstractNacosHttpServerTestExecutionListener {

    public static final String DATA_ID = "app";

    private static final String APP_NAME = "Nacos-Spring";

    private static final String ANOTHER_APP_NAME = "Nacos-Spring-1";


    @Override
    public void init(EmbeddedNacosHttpServer httpServer) {
        Map<String, String> config = new HashMap<String, String>(1);
        config.put(DATA_ID_PARAM_NAME, DATA_ID);
        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
        config.put(CONTENT_PARAM_NAME, "app.name=" + APP_NAME);
        httpServer.initConfig(config);
    }

    @Override
    protected String getServerAddressPropertyName() {
        return "server.addr";
    }


    public static class App {
        @Value("${app.name}")
        private String name;

        @NacosValue("${app.name}")
        private String nacosName;

        public void setName(String name) {
            this.name = name;
        }

        public void setNacosName(String nacosName) {
            this.nacosName = nacosName;
        }
    }

    @Bean
    public App app() {
        return new App();
    }

    @NacosInjected
    private ConfigService configService;

    @Autowired
    private App app;

    @Autowired
    private Environment environment;

    @Test
    public void testValue() throws NacosException, InterruptedException {
        Assert.assertEquals(APP_NAME, app.name);

        Assert.assertEquals(APP_NAME, app.nacosName);

        Assert.assertEquals(APP_NAME, environment.getProperty("app.name"));

        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "app.name=" + ANOTHER_APP_NAME);

        Thread.sleep(1000);

        Assert.assertEquals(APP_NAME, app.name);

        Assert.assertEquals(ANOTHER_APP_NAME, environment.getProperty("app.name"));

        Assert.assertEquals(ANOTHER_APP_NAME, app.nacosName);
    }

}
