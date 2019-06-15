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
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource;

import java.util.Properties;

/**
 * {@link ConfigService} Bean Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class ConfigServiceBeanBuilder extends AbstractNacosServiceBeanBuilder<ConfigService> {

    /**
     * The bean name of {@link ConfigServiceBeanBuilder}
     */
    public static final String BEAN_NAME = "configServiceBeanBuilder";

    protected ConfigServiceBeanBuilder() {
        super(GlobalNacosPropertiesSource.CONFIG);
    }

    @Override
    protected ConfigService createService(NacosServiceFactory nacosServiceFactory, Properties properties)
            throws NacosException {
        return nacosServiceFactory.createConfigService(properties);
    }
}
