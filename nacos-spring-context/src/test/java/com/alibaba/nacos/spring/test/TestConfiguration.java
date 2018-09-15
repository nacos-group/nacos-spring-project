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
package com.alibaba.nacos.spring.test;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.*;

/**
 * Test {@link Configuration @Configuration} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@Configuration
public class TestConfiguration {

    /**
     * The bean name of {@link ConfigService}
     */
    public static final String CONFIG_SERVICE_BEAN_NAME = "configService";

    public static final String TEST_CONFIG = "id = 1\n" +
            "name = mercyblitz\n" +
            "value = 0.95\n" +
            "intData  = 1234\n" +
            "float-data = 1234.5";

    public static final String MODIFIED_TEST_CONTEXT = "id = 1\n" +
            "name = mercyblitz@gmail.com\n" +
            "value = 9527\n";

    @Bean(name = GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    public Properties globalNacosProperties() {
        Properties properties = new Properties();
        return properties;
    }

    @Bean(name = NACOS_SERVICE_FACTORY_BEAN_NAME)
    public NacosServiceFactory nacosServiceFactory(ListableBeanFactory beanFactory) {

        MockNacosServiceFactory nacosServiceFactory = new MockNacosServiceFactory();

        Map<String, ConfigService> configServices = beanFactory.getBeansOfType(ConfigService.class);

        if (configServices.containsKey(CONFIG_SERVICE_BEAN_NAME)) {
            ConfigService configService = configServices.get(CONFIG_SERVICE_BEAN_NAME);
            nacosServiceFactory.setConfigService(configService);
        }

        return nacosServiceFactory;
    }

    @Bean(name = NACOS_CONFIG_LISTENER_EXECUTOR_BEAN_NAME)
    public ExecutorService executorService() {
        return Executors.newSingleThreadExecutor();
    }

}
