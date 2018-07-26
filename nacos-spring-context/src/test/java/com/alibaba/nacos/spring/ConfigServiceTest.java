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
package com.alibaba.nacos.spring;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.annotation.NacosService;
import org.junit.Test;

import java.util.Properties;

/**
 * TODO
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
public class ConfigServiceTest {

    @NacosService(serverAddr = "${serverAddr}")
    private ConfigService configService;

    @Test
    public void testConfigService() throws NacosException {
        String dataId = "testDataId";
        String group = "testGroupId";
        Properties properties = new Properties();
        configService.publishConfig(dataId, group, "Hello,World");
        // Actively get the configuration.
        String content = configService.getConfig(dataId, group, 5000);
        System.out.println(content);
    }

}
