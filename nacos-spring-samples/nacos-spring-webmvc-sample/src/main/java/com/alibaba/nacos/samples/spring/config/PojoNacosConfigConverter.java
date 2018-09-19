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
package com.alibaba.nacos.samples.spring.config;

import com.alibaba.nacos.api.config.convert.NacosConfigConverter;
import com.alibaba.nacos.samples.spring.domain.Pojo;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * {@link Pojo} Jackson {@link NacosConfigConverter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class PojoNacosConfigConverter implements NacosConfigConverter<Pojo> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canConvert(Class<Pojo> targetType) {
        return objectMapper.canSerialize(targetType);
    }

    @Override
    public Pojo convert(String config) {
        try {
            return objectMapper.readValue(config, Pojo.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
