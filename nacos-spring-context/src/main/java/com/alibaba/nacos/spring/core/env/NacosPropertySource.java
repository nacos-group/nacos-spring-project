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
package com.alibaba.nacos.spring.core.env;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosUtils.toProperties;

/**
 * Nacos {@link PropertySource}, all read methods are immutable.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySource extends PropertiesPropertySource {

    private String groupId;

    private String dataId;

    private boolean autoRefreshed;

    private boolean first;

    private String before;

    private String after;

    private String type;

    private Properties properties;

    private Map<String, Object> attributesMetadata;

    private Object origin;

    private String beanName;

    private Class<?> beanType;

    /**
     * @param name        the name of Nacos {@link PropertySource}
     * @param nacosConfig the Nacos Config with {@link Properties} format
     */
    public NacosPropertySource(String dataId, String groupId, String name, String nacosConfig, String type) {
        super(name, toProperties(dataId, groupId, nacosConfig, type));
        this.type = type;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public boolean isAutoRefreshed() {
        return autoRefreshed;
    }

    public void setAutoRefreshed(boolean autoRefreshed) {
        this.autoRefreshed = autoRefreshed;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * @param attributesMetadata the attributesMetadata of attributes from
     *                           {@link com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource @NacosPropertySource}
     *                           or &lt;nacos:property-source ... &gt;
     */
    public void setAttributesMetadata(Map<String, Object> attributesMetadata) {
        this.attributesMetadata = attributesMetadata;
    }

    /**
     * @param origin where Nacos {@link PropertySource} comes from
     */
    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    /**
     * @return the attributesMetadata of attributes from
     * {@link com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource @NacosPropertySource}
     * or &lt;nacos:property-source ... &gt;
     */
    public Map<String, Object> getAttributesMetadata() {
        return attributesMetadata != null ? attributesMetadata : Collections.<String, Object>emptyMap();
    }

    /**
     * @return where Nacos {@link PropertySource} comes from
     */
    public Object getOrigin() {
        return origin;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public void setBeanType(Class<?> beanType) {
        this.beanType = beanType;
    }

    protected void copy(NacosPropertySource original) {
        this.groupId = original.groupId;
        this.dataId = original.dataId;
        this.autoRefreshed = original.autoRefreshed;
        this.first = original.first;
        this.before = original.before;
        this.after = original.after;
        this.type = original.type;
        this.properties = original.properties;
        this.attributesMetadata = original.attributesMetadata;
        this.origin = original.origin;
        this.beanName = original.beanName;
        this.beanType = original.beanType;
    }
}
