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
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_TIMEOUT;
import static com.alibaba.nacos.spring.util.NacosUtils.identify;

/**
 * Mock {@link NacosServiceFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class MockNacosServiceFactory implements NacosServiceFactory {

    public final static String DATA_ID = "testDataId";

    public final static String GROUP_ID = "testGroupId";

    public final static String CONTENT = "Hello,World 2018";

    private String dataId;

    private String groupId;

    private long timeout;

    private String content;

    private ConfigService configService;

    private Map<String, ConfigService> configServiceCache = new HashMap<String, ConfigService>();

    public MockNacosServiceFactory() {
        this(DATA_ID, GROUP_ID, DEFAULT_TIMEOUT, CONTENT);
    }

    public MockNacosServiceFactory(String dataId, String groupId, long timeout, String content) {
        this.dataId = dataId;
        this.groupId = groupId;
        this.timeout = timeout;
        this.content = content;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public ConfigService createConfigService(Properties properties) throws NacosException {
        if (configService != null) {
            return configService;
        }
        String key = identify(properties);
        ConfigService configService = configServiceCache.get(key);
        if (configService == null) {
            configService = new MockConfigService();
            configServiceCache.put(key, configService);
        }
        return configService;
    }

    @Override
    public NamingService createNamingService(Properties properties) throws NacosException {
        return Mockito.mock(NamingService.class);
    }
}
