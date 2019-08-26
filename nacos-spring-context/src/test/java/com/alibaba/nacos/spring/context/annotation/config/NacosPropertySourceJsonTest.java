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
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.CONTENT_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.DATA_ID_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.GROUP_ID_PARAM_NAME;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        NacosPropertySourceJsonTest.class
})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, NacosPropertySourceJsonTest.class})
@NacosPropertySource(dataId = NacosPropertySourceJsonTest.DATA_ID, autoRefreshed = true, type = ConfigType.JSON)
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
@Component
public class NacosPropertySourceJsonTest extends AbstractNacosHttpServerTestExecutionListener {

    public static final String DATA_ID = "data_json";

    @Override
    protected String getServerAddressPropertyName() {
        return "server.addr";
    }

    private String configStr = "{\n" +
            "    \"people\":{\n" +
            "        \"a\":\"liaochuntao\",\n" +
            "        \"b\":\"this is test\"\n" +
            "    }\n" +
            "}";

    private String newConfigStr = "{\n" +
            "    \"people\":{\n" +
            "        \"a\":\"liaochuntao\",\n" +
            "        \"b\":\"refresh this is test\"\n" +
            "    }\n" +
            "}";

    @Override
    public void init(EmbeddedNacosHttpServer httpServer) {
        Map<String, String> config = new HashMap<String, String>(1);
        config.put(DATA_ID_PARAM_NAME, DATA_ID);
        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
        config.put(CONTENT_PARAM_NAME, configStr);

        httpServer.initConfig(config);
    }

    @NacosInjected
    private ConfigService configService;

    public static class App {

        @Value("${people.a}")
        private String a;
        @NacosValue(value = "${people.b}", autoRefreshed = true)
        private String b;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }
    }

    @Bean
    public App app() {
        return new App();
    }

    @Autowired
    private App app;

    @Test
    public void testValue() throws NacosException, InterruptedException {

        Assert.assertEquals("liaochuntao", app.a);
        Assert.assertEquals("this is test", app.b);

        configService.publishConfig(DATA_ID, DEFAULT_GROUP, newConfigStr);

        Thread.sleep(2000);

        Assert.assertEquals("liaochuntao", app.a);
        Assert.assertEquals("refresh this is test", app.b);


    }

}
