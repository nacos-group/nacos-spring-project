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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * Nacos Configuration Loader
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigLoader {

    private final ConfigurableEnvironment environment;

    private final ConversionService conversionService;

    private final PropertiesPlaceholderResolver resolver;

    private NacosServiceFactory nacosServiceFactory;

    private ConfigService configService;

    public NacosConfigLoader(ConfigurableEnvironment environment) {
        this.environment = environment;
        this.conversionService = environment.getConversionService();
        this.resolver = new PropertiesPlaceholderResolver(environment);
    }

    /**
     * Load Nacos config vid dataId, groupId and {@link NacosProperties}
     *
     * @param dataId          dataId
     * @param groupId         groupId
     * @param nacosProperties {@link NacosProperties}
     * @return Nacos config
     * @throws RuntimeException If {@link ConfigService} creating is failed.
     */
    public String load(String dataId, String groupId, NacosProperties nacosProperties) throws RuntimeException {
        Properties properties = resolver.resolve(nacosProperties);
        return load(dataId, groupId, properties);
    }

    /**
     * Load Nacos config vid dataId, groupId and {@link Properties acos Properties}
     *
     * @param dataId          dataId
     * @param groupId         groupId
     * @param nacosProperties {@link Properties acos Properties}
     * @return Nacos config
     * @throws RuntimeException If {@link ConfigService} creating is failed.
     */
    public String load(String dataId, String groupId, Properties nacosProperties) throws RuntimeException {
        try {
            configService = nacosServiceFactory != null ?
                    nacosServiceFactory.createConfigService(nacosProperties) :
                    NacosFactory.createConfigService(nacosProperties);
        } catch (NacosException e) {
            throw new RuntimeException("ConfigService can't be created with dataId :"
                    + dataId + " , groupId : " + groupId + " , properties : " + nacosProperties
                    , e);
        }
        return NacosUtils.getContent(configService, dataId, groupId);
    }

    /**
     * Load target class object from Nacos config
     *
     * @param dataId          dataId
     * @param groupId         groupId
     * @param nacosProperties {@link NacosProperties}
     * @param targetClass     target class
     * @param <T>             target class
     * @return target class object
     * @throws RuntimeException {@link #load(String, String, NacosProperties)}
     * @see #load(String, String, NacosProperties)
     */
    public <T> T load(String dataId, String groupId, NacosProperties nacosProperties, Class<T> targetClass) throws RuntimeException {
        String config = load(dataId, groupId, nacosProperties);
        return conversionService.convert(config, targetClass);
    }

    public void setNacosServiceFactory(NacosServiceFactory nacosServiceFactory) {
        this.nacosServiceFactory = nacosServiceFactory;
    }

    public ConfigService getConfigService() {
        return configService;
    }
}
