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

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.context.config.xml.GlobalNacosPropertiesBeanDefinitionParser;
import com.alibaba.nacos.spring.context.config.xml.NacosAnnotationDrivenBeanDefinitionParser;
import com.alibaba.nacos.spring.context.config.xml.NacosNamespaceHandler;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.*;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;

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
        "classpath:/META-INF/nacos-context.xml",
        "classpath:/META-INF/nacos-property-source.xml"
})
@DirtiesContext
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, NacosNamespaceHandlerTest.class})
public class NacosNamespaceHandlerTest extends AbstractNacosHttpServerTestExecutionListener {

    @Autowired
    private User user;

    @Override
    protected String getServerAddressPropertyName() {
        return "nacos.server-addr";
    }

    @Override
    protected void init(EmbeddedNacosHttpServer server) {
        Map<String, String> config = new HashMap<String, String>(1);
        config.put(DATA_ID_PARAM_NAME, "user");
        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
        config.put(CONTENT_PARAM_NAME, "id=" + 1 + "\nname=mercyblitz");
        server.initConfig(config);
    }

    @NacosInjected
    private ConfigService configService;

    @Test
    public void testGetConfig() throws Exception {
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
        Assert.assertEquals("9527", configService.getConfig(DATA_ID, DEFAULT_GROUP, 5000));
    }

    @Test
    public void testUser() {
        Assert.assertEquals(Long.valueOf(1L), user.getId());
        Assert.assertEquals("mercyblitz", user.getName());
    }

}
