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

import com.alibaba.nacos.spring.util.ConfigParse;

import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.parse.DefaultYamlConfigParse.createYaml;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
public class DefaultJsonConfigParse implements ConfigParse {

    @Override
    public Properties parse(String configText) {
        final Properties result = new Properties();
        DefaultYamlConfigParse.process(new DefaultYamlConfigParse.MatchCallback() {
            @Override
            public void process(Properties properties, Map<String, Object> map) {
                result.putAll(properties);
            }
        }, createYaml(), configText);
        return result;
    }

    @Override
    public String processType() {
        return "json";
    }

    @Override
    public String dataId() {
        return null;
    }

    @Override
    public String group() {
        return null;
    }
}

