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
package com.alibaba.nacos.samples.spring.event;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.event.config.NacosConfigListenerRegisteredEvent;
import com.alibaba.nacos.spring.context.event.config.NacosConfigReceivedEvent;
import com.alibaba.nacos.spring.context.event.config.NacosConfigRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.alibaba.nacos.api.common.Constants.DATAID;
import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;

/**
 * Nacos Event/Listener {@link Configuration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@Configuration
public class NacosEventListenerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(NacosEventListenerConfiguration.class);

    private static final String DATA_ID = "event-data-id";

    @NacosInjected
    private ConfigService configService;

    @PostConstruct
    public void init() throws NacosException {
        // for NacosConfigReceivedEvent
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "Hello,World");

        // for NacosConfigRemovedEvent
        configService.removeConfig(DATA_ID, DEFAULT_GROUP);

        Listener listener = new AbstractListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
            }
        };

        // for NacosConfigListenerRegisteredEvent(true)
        configService.addListener(DATAID, DEFAULT_GROUP, listener);

        // for NacosConfigListenerRegisteredEvent(false)
        configService.removeListener(DATAID, DEFAULT_GROUP, listener);
    }

    @Bean
    public ApplicationListener<NacosConfigReceivedEvent> nacosConfigReceivedEventListener() {
        return new ApplicationListener<NacosConfigReceivedEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigReceivedEvent event) {
                logger.info("Listening on NacosConfigReceivedEvent -  dataId : {} , groupId : {} , " + "content : {} , "
                        + "source : {}", event.getDataId(), event.getGroupId(), event.getContent(), event.getSource());
            }
        };
    }

    @Bean
    public ApplicationListener<NacosConfigRemovedEvent> nacosConfigRemovedEventListener() {
        return new ApplicationListener<NacosConfigRemovedEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigRemovedEvent event) {
                logger.info("Listening on NacosConfigRemovedEvent -  dataId : {} , groupId : {} , " + "removed : {} , "
                        + "source : {}", event.getDataId(), event.getGroupId(), event.isRemoved(), event.getSource());
            }
        };
    }

    @Bean
    public ApplicationListener<NacosConfigListenerRegisteredEvent> nacosConfigListenerRegisteredEventListener() {
        return new ApplicationListener<NacosConfigListenerRegisteredEvent>() {
            @Override
            public void onApplicationEvent(NacosConfigListenerRegisteredEvent event) {
                logger.info("Listening on NacosConfigListenerRegisteredEvent -  dataId : {} , groupId : {} , " + "registered : {} , "
                        + "source : {}", event.getDataId(), event.getGroupId(), event.isRegistered(), event.getSource());
            }
        };
    }
}
