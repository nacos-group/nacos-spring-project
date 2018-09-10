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

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.spring.beans.factory.annotation.AnnotationNacosInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.test.ListenersConfiguration;
import com.alibaba.nacos.spring.test.TestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;

/**
 * {@link NacosConfigListenerMethodProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        TestConfiguration.class,
        ListenersConfiguration.class,
        ConfigServiceBeanBuilder.class,
        AnnotationNacosInjectedBeanPostProcessor.class,
        NacosConfigListenerMethodProcessor.class,
        NacosConfigListenerMethodProcessorTest.class,

})
public class NacosConfigListenerMethodProcessorTest {

    @NacosInjected
    private ConfigService configService;

    @Test
    public void testOn() throws Exception {

        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "9527");
        // Publish User
        configService.publishConfig("user", DEFAULT_GROUP, "{\"id\":1,\"name\":\"mercyblitz\"}");

        Thread.sleep(1000);

    }

}
