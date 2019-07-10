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
package com.alibaba.nacos.spring.context.event.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.event.DeferredApplicationEventPublisher;
import com.alibaba.nacos.spring.metadata.NacosServiceMetaData;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * {@link NacosConfigEvent Event} publishing {@link ConfigService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class EventPublishingConfigService implements ConfigService, NacosServiceMetaData {

    private final ConfigService configService;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Executor executor;

    private final Properties properties;

    public EventPublishingConfigService(ConfigService configService, Properties properties, ConfigurableApplicationContext context,
                                        Executor executor) {
        this.configService = configService;
        this.properties = properties;
        this.applicationEventPublisher = new DeferredApplicationEventPublisher(context);
        this.executor = executor;
    }

    @Override
    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
        try {
            return configService.getConfig(dataId, group, timeoutMs);
        } catch (NacosException e) {
            if (NacosException.SERVER_ERROR == e.getErrCode()) { // timeout error
                publishEvent(new NacosConfigTimeoutEvent(configService, dataId, group, timeoutMs, e.getErrMsg()));
            }
            throw e; // re-throw NacosException
        }
    }

    @Override
    public String getConfigAndSignListener(String dataId, String group, long timeoutMs, Listener listener) throws NacosException {
        Listener listenerAdapter = new DelegatingEventPublishingListener(configService, dataId, group, applicationEventPublisher, executor, listener);
        return configService.getConfigAndSignListener(dataId, group, timeoutMs, listenerAdapter);
    }

    /**
     * Implementation of the new version of support for multiple configuration file type resolution
     *
     * @param dataId dataId
     * @param group group
     * @param type config's type
     * @param listener listener
     * @throws NacosException
     */
    public void addListener(String dataId, String group, String type, Listener listener) throws NacosException {
        Listener listenerAdapter = new DelegatingEventPublishingListener(configService, dataId, group, type, applicationEventPublisher, executor, listener);
        addListener(dataId, group, listenerAdapter);
    }

    @Override
    public void addListener(String dataId, String group, Listener listener) throws NacosException {
        configService.addListener(dataId, group, listener);
        publishEvent(new NacosConfigListenerRegisteredEvent(configService, dataId, group, listener, true));

    }

    @Override
    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        boolean published = configService.publishConfig(dataId, group, content);
        publishEvent(new NacosConfigPublishedEvent(configService, dataId, group, content, published));
        return published;
    }

    @Override
    public boolean removeConfig(String dataId, String group) throws NacosException {
        boolean removed = configService.removeConfig(dataId, group);
        publishEvent(new NacosConfigRemovedEvent(configService, dataId, group, removed));
        return removed;
    }

    @Override
    public void removeListener(String dataId, String group, Listener listener) {
        configService.removeListener(dataId, group, listener);
        publishEvent(new NacosConfigListenerRegisteredEvent(configService, dataId, group, listener, false));
    }

    @Override
    public String getServerStatus() {
        return configService.getServerStatus();
    }

    private void publishEvent(NacosConfigEvent nacosConfigEvent) {
        applicationEventPublisher.publishEvent(nacosConfigEvent);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
