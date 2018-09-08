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
package com.alibaba.nacos.spring.context.config;

import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import com.alibaba.nacos.spring.test.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.test.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
 * {@link NacosPropertySourceBeanDefinitionParser} Test
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
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class, NacosPropertySourceBeanDefinitionParserTest.class})
public class NacosPropertySourceBeanDefinitionParserTest extends AbstractNacosHttpServerTestExecutionListener {

    private static final Long USER_ID = 1991L;
    private static final String USER_NAME = "hxy";

    @Override
    protected void init(EmbeddedNacosHttpServer server) {
        Map<String, String> config = new HashMap<String, String>(1);
        config.put(DATA_ID_PARAM_NAME, "user");
        config.put(GROUP_ID_PARAM_NAME, DEFAULT_GROUP);
        config.put(CONTENT_PARAM_NAME, "id=" + USER_ID + "\nname=" + USER_NAME);
        server.initConfig(config);
    }

    @Override
    protected String getServerAddressPropertyName() {
        return "nacos.server-addr";
    }

    @Autowired
    private User user;

    @Test
    public void testGetConfig() {
        Assert.assertEquals(USER_ID, user.getId());
        Assert.assertEquals(USER_NAME, user.getName());
    }

}
