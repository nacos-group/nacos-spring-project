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
package com.alibaba.nacos.spring.context.annotation.config;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.springframework.core.env.PropertySource;

import java.lang.annotation.*;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_BOOLEAN_ATTRIBUTE_VALUE;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_CONFIG_TYPE_VALUE;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_STRING_ATTRIBUTE_VALUE;

/**
 * An annotation for Nacos {@link PropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySource
 * @see org.springframework.context.annotation.PropertySource
 * @since 0.1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(NacosPropertySources.class)
public @interface NacosPropertySource {

    /**
     * The attribute name of {@link NacosPropertySource#name()}
     */
    String NAME_ATTRIBUTE_NAME = "name";

    /**
     * The attribute name of {@link NacosPropertySource#groupId()}
     */
    String GROUP_ID_ATTRIBUTE_NAME = "groupId";

    /**
     * The attribute name of {@link NacosPropertySource#dataId()}
     */
    String DATA_ID_ATTRIBUTE_NAME = "dataId";

    /**
     * The attribute name of {@link NacosPropertySource#autoRefreshed()}
     */
    String AUTO_REFRESHED_ATTRIBUTE_NAME = "autoRefreshed";

    /**
     * The attribute name of {@link NacosPropertySource#first()}
     */
    String FIRST_ATTRIBUTE_NAME = "first";

    /**
     * The attribute name of {@link NacosPropertySource#before()}
     */
    String BEFORE_ATTRIBUTE_NAME = "before";

    /**
     * The attribute name of {@link NacosPropertySource#after()}
     */
    String AFTER_ATTRIBUTE_NAME = "after";

    /**
     * The attribute name of {@link NacosPropertySource#properties()}
     */
    String PROPERTIES_ATTRIBUTE_NAME = "properties";

    /**
     * The attribute name of {@link NacosPropertySource#type()} ()}
     */
    String CONFIG_TYPE_ATTRIBUTE_NAME = "type";

    /**
     * The name of Nacos {@link PropertySource}
     * If absent , the default name will be built from
     * {@link #dataId() dataId}, {@link #groupId() groupid} and {@link #properties() properties} by
     * {@link NacosUtils#buildDefaultPropertySourceName(String, String, Map)} method
     *
     * @return default value is ""
     */
    String name() default DEFAULT_STRING_ATTRIBUTE_VALUE;

    /**
     * Nacos Group ID
     *
     * @return default value {@link Constants#DEFAULT_GROUP};
     */
    String groupId() default DEFAULT_GROUP;

    /**
     * Nacos Data ID
     *
     * @return required value.
     */
    String dataId();

    /**
     * It indicates the property source is auto-refreshed when Nacos configuration is changed.
     *
     * @return default value is <code>false</code>
     */
    boolean autoRefreshed() default DEFAULT_BOOLEAN_ATTRIBUTE_VALUE;

    /**
     * Indicates current Nacos {@link PropertySource} is first order or not
     * If specified , {@link #before()} and {@link #after()} will be ignored, or
     * last order.
     *
     * @return default value is <code>false</code>
     */
    boolean first() default DEFAULT_BOOLEAN_ATTRIBUTE_VALUE;

    /**
     * The relative order before specified {@link PropertySource}
     * <p>
     * If not specified , current Nacos {@link PropertySource} will be added last.
     * <p>
     * If {@link #first()} specified , current attribute will be ignored.
     *
     * @return the name of {@link PropertySource}
     */
    String before() default DEFAULT_STRING_ATTRIBUTE_VALUE;

    /**
     * The relative order after specified {@link PropertySource}
     * <p>
     * If not specified , current Nacos {@link PropertySource} will be added last.
     * <p>
     * If {@link #first()} specified , current attribute will be ignored.
     *
     * @return the name of {@link PropertySource}
     */
    String after() default DEFAULT_STRING_ATTRIBUTE_VALUE;

    /**
     * The type of config
     *
     * @return the type of config
     */
    ConfigType type() default ConfigType.PROPERTIES;

    /**
     * The {@link NacosProperties} attribute, If not specified, it will use
     * {@link EnableNacos#globalProperties() global Nacos Properties}.
     *
     * @return the default value is {@link NacosProperties}
     * @see EnableNacos#globalProperties()
     */
    NacosProperties properties() default @NacosProperties;


}
