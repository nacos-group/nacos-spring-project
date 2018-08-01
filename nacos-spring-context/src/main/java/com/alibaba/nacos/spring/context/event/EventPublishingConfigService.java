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
package com.alibaba.nacos.spring.context.event;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.context.ApplicationEventPublisher;

/**
 * {@link NacosConfigEvent Event} publishing {@link ConfigService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class EventPublishingConfigService implements ConfigService {

    private final ConfigService configService;

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublishingConfigService(ConfigService configService,
                                        ApplicationEventPublisher applicationEventPublisher) {
        this.configService = configService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
        return configService.getConfig(dataId, group, timeoutMs);
    }

    @Override
    public void addListener(String dataId, String group, Listener listener) throws NacosException {
        configService.addListener(dataId, group, listener);
    }

    @Override
    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        boolean published = configService.publishConfig(dataId, group, content);
        NacosConfigPublishedEvent event = new NacosConfigPublishedEvent(configService, dataId, group, content, published);
        applicationEventPublisher.publishEvent(event);
        return published;
    }

    @Override
    public boolean removeConfig(String dataId, String group) throws NacosException {
        boolean removed = configService.removeConfig(dataId, group);
        NacosConfigRemovedEvent event = new NacosConfigRemovedEvent(configService, dataId, group, removed);
        applicationEventPublisher.publishEvent(event);
        return removed;
    }

    @Override
    public void removeListener(String dataId, String group, Listener listener) {
        configService.removeListener(dataId, group, listener);
    }
}
