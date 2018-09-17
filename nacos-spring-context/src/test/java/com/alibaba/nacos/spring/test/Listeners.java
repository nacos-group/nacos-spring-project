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
package com.alibaba.nacos.spring.test;

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.nacos.spring.test.MockNacosServiceFactory.DATA_ID;

/**
 * {@link NacosConfigListener} {@link Configuration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
@Configuration
public class Listeners {

    private Integer integerValue;

    private Double doubleValue;

    @NacosConfigListener(dataId = DATA_ID, timeout = 50)
    public void onInteger(Integer value) throws Exception {
        Thread.sleep(100); // timeout of execution
        this.integerValue = value;
    }

    @NacosConfigListener(dataId = DATA_ID, timeout = 200)
    public void onDouble(Double value) throws Exception {
        Thread.sleep(100); // normal execution
        this.doubleValue = value;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }
}
