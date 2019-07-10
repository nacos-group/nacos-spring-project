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

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.spring.factory.CacheableEventPublishingNacosServiceFactory;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.GLOBAL_NACOS_PROPERTIES_BEAN_NAME;

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

    public static final String TEST_CONFIG = "test.id = 1\n" +
            "test.name = mercyblitz\n" +
            "test.value = 0.95\n" +
            "test.intData  = 1234\n" +
            "test.float-data = 1234.5\n" +
            "test.list = 1,2,3,4,5\n" +
            "test.map[key-1]=value";

    public static final String MODIFIED_TEST_CONTEXT = "id = 1\n" +
            "test.name = mercyblitz@gmail.com\n" +
            "test.value = 9527\n" +
            "test.list[0] = 6\n" +
            "test.list[1] = 6\n" +
            "test.list[2] = 6\n" +
            "test.list[3] = 6\n" +
            "test.map[key-2]=value\n" +
            "test.map[key-3]=value";


    @Bean(name = GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    public Properties globalNacosProperties() {
        Properties properties = new Properties();
        if (!StringUtils.isEmpty(System.getProperty("server.addr"))) {
            properties.put(PropertyKeyConst.SERVER_ADDR, System.getProperty("server.addr"));
        }
        return properties;
    }

}
