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
package com.alibaba.nacos.spring.context.properties;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * {@link NacosConfigurationProperties} Bean Binder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigurationPropertiesBinder {

    private static final Logger logger = LoggerFactory.getLogger(NacosConfigurationPropertiesBinder.class);

    private final ConfigService configService;

    public NacosConfigurationPropertiesBinder(ConfigService configService) {
        Assert.notNull(configService, "ConfigService must not be null!");
        this.configService = configService;
    }

    public void bind(Object bean) {

        NacosConfigurationProperties properties = findAnnotation(bean.getClass(), NacosConfigurationProperties.class);

        bind(bean, properties);

    }

    public void bind(final Object bean, final NacosConfigurationProperties properties) {

        Assert.notNull(bean, "Bean must not be null!");

        Assert.notNull(properties, "NacosConfigurationProperties must not be null!");

        String dataId = properties.dataId();

        String groupId = properties.groupId();

        if (properties.autoRefreshed()) { // Add a Listener if auto-refreshed

            try {
                configService.addListener(dataId, groupId, new AbstractListener() {

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        doBind(bean, properties, configInfo);
                    }
                });
            } catch (NacosException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        final String content = NacosUtils.getContent(configService, dataId, groupId);

        if (!StringUtils.hasText(content)) {
            // ignore when content is blank
            return;
        }

        doBind(bean, properties, content);

    }

    protected void doBind(Object bean, NacosConfigurationProperties properties, String content) {

        DataBinder dataBinder = new DataBinder(bean);

        dataBinder.setAutoGrowNestedPaths(properties.ignoreNestedProperties());
        dataBinder.setIgnoreInvalidFields(properties.ignoreInvalidFields());
        dataBinder.setIgnoreUnknownFields(properties.ignoreUnknownFields());

        try {
            Properties props = new Properties();
            props.load(new StringReader(content));
            dataBinder.bind(new MutablePropertyValues(props));
        } catch (IOException e) {
            if (properties.exceptionIfInvalid()) {
                throw new IllegalStateException(e);
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

}
