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
package com.alibaba.nacos.samples.spring.domain;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.config.annotation.NacosIgnore;
import com.alibaba.nacos.api.config.annotation.NacosProperty;

import java.util.Date;

import static com.alibaba.nacos.samples.spring.domain.Pojo.DATA_ID;

/**
 * POJO
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@NacosConfigurationProperties(dataId = DATA_ID, autoRefreshed = true)
public class Pojo {

    public static final String DATA_ID = "pojo-data-id";

    private Long id;

    private String name;

    private Date created;

    @NacosProperty("desc")
    private String description;

    @NacosIgnore
    private boolean ignored;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    @Override
    public String toString() {
        return "Pojo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", created=" + created +
                ", description='" + description + '\'' +
                ", ignored=" + ignored +
                '}';
    }
}
