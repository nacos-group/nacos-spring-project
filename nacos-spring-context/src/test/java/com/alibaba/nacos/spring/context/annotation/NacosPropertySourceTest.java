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
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.EmbeddedNacosHttpServer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
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

@NacosPropertySource(dataId = NacosPropertySourceTest.DATA_ID)
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
@Component
public class NacosPropertySourceTest extends AbstractNacosHttpServerTestExecutionListener {

    public static final String DATA_ID = "app";

    private static final String APP_NAME = "Nacos-Spring";


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

    @Value("${app.name}")
    private String appName;


    @Test
    public void testValue() {
        Assert.assertEquals(APP_NAME, appName);
    }

}
