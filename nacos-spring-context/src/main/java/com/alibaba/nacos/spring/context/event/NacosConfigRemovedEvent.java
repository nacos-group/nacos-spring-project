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

/**
 * The {@link NacosConfigEvent event} of Nacos Configuration that has been removed.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigRemovedEvent extends NacosConfigEvent {

    private final boolean removed;

    public NacosConfigRemovedEvent(ConfigService configService, String dataId, String groupId, boolean removed) {
        super(configService, dataId, groupId);
        this.removed = removed;
    }

    /**
     * Is removed or not from {@link ConfigService#removeConfig(String, String)} method executing result.
     *
     * @return if removed , return <code>true</code>
     */
    public boolean isRemoved() {
        return removed;
    }
}
