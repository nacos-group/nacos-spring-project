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
package com.alibaba.nacos.spring.util;

import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.test.TestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.GLOBAL_NACOS_PROPERTIES_BEAN_NAME;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.isBeanDefinitionPresent;

/**
 * {@link NacosBeanUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class NacosBeanUtilsTest {

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    @Qualifier(GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    private Properties globalProperties;

    @Autowired
    @Qualifier(CacheableEventPublishingNacosServiceFactory.BEAN_NAME)
    private NacosServiceFactory nacosServiceFactory;

    @Test
    public void testBeans() {

        Assert.assertEquals(globalProperties, NacosBeanUtils.getGlobalPropertiesBean(beanFactory));
        Assert.assertEquals(nacosServiceFactory, NacosBeanUtils.getNacosServiceFactoryBean(beanFactory));

    }

    @Test
    public void testIsBeanDefinitionPresent() {

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        Assert.assertTrue(isBeanDefinitionPresent(registry, GLOBAL_NACOS_PROPERTIES_BEAN_NAME, Properties.class));
        Assert.assertTrue(isBeanDefinitionPresent(registry, CacheableEventPublishingNacosServiceFactory.BEAN_NAME, NacosServiceFactory.class));
        Assert.assertTrue(isBeanDefinitionPresent(registry, CacheableEventPublishingNacosServiceFactory.BEAN_NAME, NacosServiceFactory.class));

    }

}
