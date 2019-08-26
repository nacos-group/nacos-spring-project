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
package com.alibaba.nacos.spring.convert.converter.config;

import com.alibaba.nacos.api.config.convert.NacosConfigConverter;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

/**
 * Default {@link NacosConfigConverter} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class DefaultNacosConfigConverter<T> implements NacosConfigConverter<T> {

    private final Class<T> targetType;

    private final ConversionService conversionService;

    private final String type;

    public DefaultNacosConfigConverter(Class<T> targetType) {
        this(targetType, new DefaultFormattingConversionService(), "properties");
    }

    public DefaultNacosConfigConverter(Class<T> targetType, ConversionService conversionService, String type) {
        this.targetType = targetType;
        this.conversionService = conversionService;
        this.type = type;
    }

    @Override
    public T convert(String source) {
        if (conversionService.canConvert(source.getClass(), targetType)) {
            return conversionService.convert(source, targetType);
        }
        return null;
    }

    @Override
    public boolean canConvert(Class<T> targetType) {
        return conversionService.canConvert(String.class, targetType);
    }
}
