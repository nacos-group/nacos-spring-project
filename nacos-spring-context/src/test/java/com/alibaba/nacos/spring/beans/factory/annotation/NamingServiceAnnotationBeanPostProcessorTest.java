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
package com.alibaba.nacos.spring.beans.factory.annotation;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.context.annotation.NacosService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * {@link NamingServiceAnnotationBeanPostProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        NamingServiceAnnotationBeanPostProcessor.class,
        NamingServiceAnnotationBeanPostProcessorTest.class
})
public class NamingServiceAnnotationBeanPostProcessorTest {

    @NacosService(serverAddr = "11.163.128.36")
    private ConfigService configService;

    @NacosService(serverAddr = "11.163.128.36")
    private ConfigService configService2;

    @NacosService(serverAddr = "11.163.128.36")
    private NamingService namingService;

    @NacosService(serverAddr = "11.163.128.36")
    private NamingService namingService2;

    String dataId = "testDataId";
    String groupId = "testGroupId";

    @Test
    public void testInjection() {
        Assert.assertEquals(configService, configService2);
        Assert.assertEquals(namingService, namingService2);
    }

    @Test
    public void test() throws NacosException {

        String content = "Hello,World 2018";

        configService.publishConfig(dataId, groupId, content);

        Assert.assertEquals(content, configService.getConfig(dataId, groupId, 5000));

    }
}
