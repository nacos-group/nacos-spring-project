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
package com.alibaba.nacos.samples.spring.listener;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.samples.spring.NacosConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.samples.spring.NacosConfiguration.CURRENT_TIME_DATA_ID;

/**
 * Timeout {@link NacosConfigListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosConfiguration
 * @since 0.1.0
 */
@Configuration
public class TimeoutNacosConfigListener {

    private static final Logger logger = LoggerFactory.getLogger(TimeoutNacosConfigListener.class);

    public static final String TEST_DATA_ID = "timeout-data-id";

    @NacosInjected
    private ConfigService configService;

    @PostConstruct
    public void init() throws Exception {
        configService.publishConfig(TEST_DATA_ID, DEFAULT_GROUP, "Hello,World");
    }

    @NacosConfigListener(dataId = CURRENT_TIME_DATA_ID, timeout = 100)
    public void onReceivedWithTimeout(String value) throws InterruptedException {
        Thread.sleep(200);
        logger.info("onReceivedWithTimeout(String) : {}", value); // Never executes
    }

    @NacosConfigListener(dataId = CURRENT_TIME_DATA_ID, timeout = 100)
    public void onReceivedWithoutTimeout(String value) throws InterruptedException {
        Thread.sleep(50);
        logger.info("onReceivedWithoutTimeout(String) : {}", value);
    }

}
