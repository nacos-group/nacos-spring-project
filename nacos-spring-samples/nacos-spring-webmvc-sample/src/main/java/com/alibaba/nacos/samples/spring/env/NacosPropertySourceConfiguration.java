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
package com.alibaba.nacos.samples.spring.env;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.samples.spring.env.NacosPropertySourceConfiguration.*;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

/**
 * {@link NacosPropertySource} {@link Configuration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@Configuration
@NacosPropertySource(name = "first", dataId = FIRST_DATA_ID, first = true, autoRefreshed = true) // First PropertySource
@NacosPropertySources({
        @NacosPropertySource(name = "before-os-env", dataId = BEFORE_OS_ENV_DATA_ID, before = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME),
        @NacosPropertySource(name = "after-system-properties", dataId = AFTER_SYS_PROP_DATA_ID, after = SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)
})
public class NacosPropertySourceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(NacosPropertySourceConfiguration.class);

    public static final String FIRST_DATA_ID = "first-property-source-data-id";

    public static final String BEFORE_OS_ENV_DATA_ID = "before-os-env-property-source-data-id";

    public static final String AFTER_SYS_PROP_DATA_ID = "after-system-properties-property-source-data-id";

    static {
        String serverAddr = System.getProperty("nacos.server-addr");
        try {
            ConfigService configService = NacosFactory.createConfigService(serverAddr);
            // Publish for FIRST_DATA_ID
            publishConfig(configService, FIRST_DATA_ID, "user.name = Mercy Ma");

            // Publish for BEFORE_OS_ENV_DATA_ID
            publishConfig(configService, BEFORE_OS_ENV_DATA_ID, "PATH = /home/my-path");

            // Publish for AFTER_SYS_PROP_DATA_ID
            publishConfig(configService, AFTER_SYS_PROP_DATA_ID, "user.name = mercyblitz");

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    private static void publishConfig(ConfigService configService, String dataId, String propertiesContent) throws NacosException {
        configService.publishConfig(dataId, DEFAULT_GROUP, propertiesContent);
    }

    /**
     * "before-os-env" overrides OS Environment variables $PATH
     */
    @Value("${PATH}")
    private String path;

    /**
     * There are three definitions of "user.name" from
     * FIRST_DATA_ID,
     * SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
     * AFTER_SYS_PROP_DATA_ID
     * <p>
     * Thus, "user.name = Mercy Ma" will be loaded from FIRST_DATA_ID, others will be ignored.
     */
    @Value("${user.name}")
    private String userName;

    @PostConstruct
    public void init() {
        logger.info("${PATH} : {}", path); // -> "home/my-path"
        logger.info("${user.name} : {}", userName); // -> "Mercy Ma"
        logger.info("Java System ${user.name} : {}", System.getProperty("user.name"));
        logger.info("OS Env ${PATH} : {}", System.getenv("PATH"));
    }

}
