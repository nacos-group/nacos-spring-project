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
package com.alibaba.nacos.spring.context.config.xml;

import com.alibaba.nacos.spring.context.config.xml.GlobalNacosPropertiesBeanDefinitionParser;
import com.alibaba.nacos.spring.context.config.xml.NacosNamespaceHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.*;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.GLOBAL_NACOS_PROPERTIES_BEAN_NAME;

/**
 * {@link GlobalNacosPropertiesBeanDefinitionParser} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosNamespaceHandler
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/nacos-global-properties.xml"
})
public class GlobalNacosPropertiesBeanDefinitionParserTest {

    @BeforeClass
    public static void init() {
        System.setProperty("nacos.server-addr", "127.0.0.1:8080");
    }

    @AfterClass
    public static void afterClass() {
        System.getProperties().remove("nacos.server-addr");
    }

    @Autowired
    @Qualifier(GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    private Properties globalNacosProperties;

    @Test
    public void test() {
        Assert.assertNull(globalNacosProperties.get(ENDPOINT));
        Assert.assertNull(globalNacosProperties.get(NAMESPACE));
        Assert.assertNull(globalNacosProperties.get(ACCESS_KEY));
        Assert.assertNull(globalNacosProperties.get(SECRET_KEY));
        Assert.assertNotNull(globalNacosProperties.get(SERVER_ADDR));
        Assert.assertNull(globalNacosProperties.get(CONTEXT_PATH));
        Assert.assertNull(globalNacosProperties.get(CLUSTER_NAME));
        Assert.assertEquals("UTF-8", globalNacosProperties.get(ENCODE));
    }


}
