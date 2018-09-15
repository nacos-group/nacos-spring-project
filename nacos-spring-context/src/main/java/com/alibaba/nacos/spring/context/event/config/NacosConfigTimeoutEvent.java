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
 * {@link NacosConfigEvent Nacos config event} for {@link ConfigService#getConfig(String, String, long) getting} timeout.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigTimeoutEvent extends NacosConfigEvent {

    private final long timeout;

    private final String errorMessage;

    /**
     * @param configService Nacos {@link ConfigService}
     * @param dataId        data ID
     * @param groupId       group ID
     * @param timeout       timeout in Millis.
     * @param errorMessage  error message
     */
    public NacosConfigTimeoutEvent(ConfigService configService, String dataId, String groupId, long timeout, String errorMessage) {
        super(configService, dataId, groupId);
        this.timeout = timeout;
        this.errorMessage = errorMessage;
    }

    /**
     * Get timeout in Millis.
     *
     * @return timeout in Millis
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * get error message
     *
     * @return error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
