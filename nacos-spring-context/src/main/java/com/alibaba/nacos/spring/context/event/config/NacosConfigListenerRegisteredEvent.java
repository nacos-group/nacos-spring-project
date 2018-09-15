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

/**
 * {@link Listener Nacos Config Listener} registered {@link NacosConfigEvent event}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Listener
 * @since 0.1.0
 */
public class NacosConfigListenerRegisteredEvent extends NacosConfigEvent {

    private final Listener listener;

    private final boolean registered;

    /**
     * @param configService Nacos {@link ConfigService}
     * @param dataId        data ID
     * @param groupId       group ID
     * @param listener      {@link Listener} instance
     * @param registered    registered or not unregistered
     */
    public NacosConfigListenerRegisteredEvent(ConfigService configService, String dataId, String groupId,
                                              Listener listener, boolean registered) {
        super(configService, dataId, groupId);
        this.listener = listener;
        this.registered = registered;
    }

    public Listener getListener() {
        return listener;
    }

    public boolean isRegistered() {
        return registered;
    }
}
