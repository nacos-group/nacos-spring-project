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
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.YamlApp;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.GROUP_ID;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        NacosPropertySourceYamlTest.class
})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, NacosPropertySourceYamlTest.class})
@NacosPropertySource(dataId = YamlApp.DATA_ID_YAML, autoRefreshed = true, type = "yaml")
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
public class NacosPropertySourceYamlTest extends AbstractNacosHttpServerTestExecutionListener {

    private String yaml = "students:\n" +
            "    - {name: lct-1,num: 12}\n" +
            "    - {name: lct-2,num: 13}\n" +
            "    - {name: lct-3,num: 14}";

    private String except = "YamlApp{students=[Student{name='lct-1', num='12'}, Student{name='lct-2', num='13'}, Student{name='lct-3', num='14'}]}";

    @Override
    public void init(EmbeddedNacosHttpServer httpServer) {
        Map<String, String> config = new HashMap<String, String>(1);
        config.put(DATA_ID_PARAM_NAME, YamlApp.DATA_ID_YAML);
        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
        config.put(CONTENT_PARAM_NAME, yaml);

        httpServer.initConfig(config);
    }

    @Bean
    public YamlApp yamlApp() {
        return new YamlApp();
    }

    @NacosInjected
    private ConfigService configService;

    @Autowired
    private YamlApp yamlApp;

    @Override
    protected String getServerAddressPropertyName() {
        return "server.addr";
    }

    @Test
    public void testValue() throws NacosException, InterruptedException {

        configService.publishConfig(YamlApp.DATA_ID_YAML, GROUP_ID, yaml);

        Thread.sleep(2000);

        System.out.println(yamlApp.toString());

        Assert.assertEquals(except, yamlApp.toString());

    }
}
