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
package com.alibaba.nacos.samples.spring;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Nacos {@link Configuration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@Configuration
@EnableNacos(
        globalProperties =
        @NacosProperties(serverAddr = "${nacos.server-addr}",
                enableRemoteSyncConfig = "true",
                maxRetry = "5",
                configRetryTime = "4000",
                configLongPollTimeout = "26000")
)
public class NacosConfiguration {

    /**
     * The data id for {@link System#currentTimeMillis()}
     */
    public static final String CURRENT_TIME_DATA_ID = "time-data-id";

    @NacosInjected
    private NamingService namingService;

    @NacosInjected(properties = @NacosProperties(encode = "UTF-8"))
    private NamingService namingServiceUTF8;

    @NacosInjected
    private ConfigService configService;


    @PostConstruct
    public void init() {
        if (namingService != namingServiceUTF8) {
            throw new RuntimeException("Why?");
        }
    }

}
