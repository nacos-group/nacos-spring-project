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

/**
 * The {@link NacosConfigEvent event} of Nacos Configuration that has been changed.
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @since 0.1.0
 */
public class NacosConfigReceivedEvent extends NacosConfigEvent {

    private final String content;
    private final String type;

    public NacosConfigReceivedEvent(ConfigService configService, String dataId, String groupId, String content, String type) {
        super(configService, dataId, groupId);
        this.content = content;
        this.type = type;
    }

    /**
     * Get Content of published Nacos Configuration
     *
     * @return content
     */
    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }
}
