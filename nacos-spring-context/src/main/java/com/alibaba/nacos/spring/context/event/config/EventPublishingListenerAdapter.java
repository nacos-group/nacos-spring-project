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
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.Executor;

/**
 * A {@link NacosConfigReceivedEvent Event} Publishing {@link Listener} adapter Nacos Config {@link Listener} with dataId, groupId and
 * {@link ConfigService} instance. A {@link NacosConfigReceivedEvent Nacos config received event} will be published when
 * a new Nacos config received.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosConfigReceivedEvent
 * @see com.alibaba.nacos.spring.context.event.config.EventPublishingConfigService
 * @see Listener
 * @since 0.1.0
 */
final class EventPublishingListenerAdapter implements Listener {

    private final ConfigService configService;

    private final String dataId;

    private final String groupId;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Listener delegate;

    EventPublishingListenerAdapter(ConfigService configService, String dataId, String groupId,
                                   ApplicationEventPublisher applicationEventPublisher, Listener delegate) {
        this.configService = configService;
        this.dataId = dataId;
        this.groupId = groupId;
        this.applicationEventPublisher = applicationEventPublisher;
        this.delegate = delegate;
    }

    @Override
    public Executor getExecutor() {
        return delegate.getExecutor();
    }

    /**
     * Callback method on Nacos config received
     *
     * @param content Nacos config
     */
    @Override
    public void receiveConfigInfo(String content) {
        NacosConfigReceivedEvent event = new NacosConfigReceivedEvent(configService, dataId, groupId, content);
        applicationEventPublisher.publishEvent(event);
        delegate.receiveConfigInfo(content);
    }
}
