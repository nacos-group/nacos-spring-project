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
package com.alibaba.nacos.spring.mock;

import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.GLOBAL_NACOS_PROPERTIES_BEAN_NAME;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.NACOS_SERVICE_FACTORY_BEAN_NAME;

/**
 * Mock {@link Configuration @Configuration} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@Configuration
public class MockConfiguration {

    @Bean(name = GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
    public Properties globalNacosProperties() {
        Properties properties = new Properties();
        return properties;
    }

    @Bean(name = NACOS_SERVICE_FACTORY_BEAN_NAME)
    public NacosServiceFactory nacosServiceFactory() {
        return new MockNacosServiceFactory();
    }

}
