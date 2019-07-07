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
package com.alibaba.nacos.spring.util.parse;

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.util.AbstractConfigParse;
import com.alibaba.nacos.spring.util.ConfigParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.0
 */
public class DefaultPropertiesConfigParse extends AbstractConfigParse {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertiesConfigParse.class);

    @Override
    public Properties parse(String configText) {
        Properties properties = new Properties();
        try {
            if (StringUtils.hasText(configText)) {
                properties.load(new StringReader(configText));
            }
        } catch (IOException e) {
            throw new ConfigParseException(e);
        }
        return properties;
    }

    @Override
    public String processType() {
        return ConfigType.PROPERTIES.getType();
    }

}

