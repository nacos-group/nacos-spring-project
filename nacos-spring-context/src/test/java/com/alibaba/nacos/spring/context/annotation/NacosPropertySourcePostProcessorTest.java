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

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.beans.factory.annotation.AnnotationNacosInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.core.env.AnnotationNacosPropertySourceBuilder;
import com.alibaba.nacos.spring.core.env.NacosPropertySourcePostProcessor;
import com.alibaba.nacos.spring.test.MockConfigService;
import com.alibaba.nacos.spring.test.TestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;
import static com.alibaba.nacos.spring.test.TestConfiguration.CONFIG_SERVICE_BEAN_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

/**
 * {@link NacosPropertySourcePostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @ee NacosPropertySourcePostProcessor
 * @since 0.1.0
 */
public class NacosPropertySourcePostProcessorTest {

    private static final String TEST_PROPERTY_NAME = "user.name";

    private static final String TEST_PROPERTY_VALUE = "mercyblitz@" + System.currentTimeMillis();

    private static final String TEST_CONTENT = TEST_PROPERTY_NAME + "=" + TEST_PROPERTY_VALUE
            + System.getProperty("line.separator")
            + "PATH = /My/Path";

    @NacosPropertySource(
            name = "second",
            dataId = DATA_ID,
            first = true,
            before = SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
            after = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)
    @NacosPropertySource(
            name = "first",
            dataId = DATA_ID,
            first = true,
            before = SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
            after = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)
    private static class FirstOrderNacosPropertySource {

    }

    @NacosPropertySource(
            dataId = DATA_ID,
            before = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            after = SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)
    private static class RelativeOrderNacosPropertySource {

    }


    @Test
    public void testFirstOrder() throws NacosException {

        AnnotationConfigApplicationContext context = createContext(DATA_ID, DEFAULT_GROUP, TEST_CONTENT);

        context.register(FirstOrderNacosPropertySource.class);

        context.refresh();

        ConfigurableEnvironment environment = context.getEnvironment();

        PropertySource propertySource = environment.getPropertySources().get("first");

        PropertySource firstPropertySource = environment.getPropertySources().iterator().next();

        Assert.assertNotNull(propertySource);

        Assert.assertEquals(propertySource, firstPropertySource);

        String systemProperty = System.getProperty(TEST_PROPERTY_NAME);

        String propertyValue = environment.getProperty(TEST_PROPERTY_NAME);

        Assert.assertNotEquals(systemProperty, propertyValue);

        Assert.assertEquals(TEST_PROPERTY_VALUE, propertyValue);

        Assert.assertEquals(TEST_PROPERTY_VALUE, propertySource.getProperty(TEST_PROPERTY_NAME));

    }

    @Test
    public void testRelativeOrder() throws NacosException {

        AnnotationConfigApplicationContext context = createContext(DATA_ID, DEFAULT_GROUP, TEST_CONTENT);

        context.register(RelativeOrderNacosPropertySource.class);

        context.refresh();

        ConfigurableEnvironment environment = context.getEnvironment();

        PropertySource propertySource = environment.getPropertySources().get("before");

        // Java System Properties before Nacos Properties
        String systemProperty = System.getProperty(TEST_PROPERTY_NAME);
        String propertyValue = environment.getProperty(TEST_PROPERTY_NAME);

        Assert.assertEquals(systemProperty, propertyValue);
        Assert.assertNotNull(TEST_PROPERTY_VALUE, propertyValue);

        // Environment Variables after Nacos Properties
        String path = System.getenv().get("PATH");
        propertyValue = environment.getProperty("PATH");

        Assert.assertNotNull(path, propertyValue);
        Assert.assertEquals("/My/Path", propertyValue);
    }

    private AnnotationConfigApplicationContext createContext(String dataId, String groupId, String content) throws NacosException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        ConfigService configService = new MockConfigService();

        configService.publishConfig(dataId, groupId, content);

        beanFactory.registerSingleton(CONFIG_SERVICE_BEAN_NAME, configService);

        context.register(TestConfiguration.class, AnnotationNacosInjectedBeanPostProcessor.class,
                NacosPropertySourcePostProcessor.class, ConfigServiceBeanBuilder.class,
                AnnotationNacosPropertySourceBuilder.class);
        return context;
    }
}
