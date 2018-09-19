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
package com.alibaba.nacos.samples.spring.properties;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.samples.spring.domain.Pojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.samples.spring.domain.Pojo.DATA_ID;

/**
 * {@link NacosConfigurationProperties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@Configuration
public class NacosConfigurationPropertiesConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(NacosConfigurationPropertiesConfiguration.class);

    @Bean
    public Pojo pojo() {
        return new Pojo();
    }

    @NacosInjected
    private ConfigService configService;

    @Autowired
    private Pojo pojo;

    @PostConstruct
    public void init() throws Exception {
        logger.info("pojo = {}", pojo);
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "id = 1");
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "name = mercyblitz");
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "desc = description");
        configService.publishConfig(DATA_ID, DEFAULT_GROUP, "ignored = true");
        logger.info("pojo.id = {}", pojo.getId());                   // 1
        logger.info("pojo.name = {}", pojo.getName());               // mercyblitz
        logger.info("pojo.description = {}", pojo.getDescription()); // description
        logger.info("pojo.ignored = {}", pojo.isIgnored());          // false
    }

}
