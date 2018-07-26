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
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.MockNacosServiceFactory;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import org.junit.Assert;
import org.junit.Test;

import static com.alibaba.nacos.client.config.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.MockNacosServiceFactory.DATA_ID;
import static com.alibaba.nacos.spring.MockNacosServiceFactory.GROUP_ID;

/**
 * {@link NacosConfigurationPropertiesBinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigurationPropertiesBinderTest {

    @NacosConfigurationProperties(dataId = DATA_ID)
    private static class Data {

        private int id;

        private String name;

        private double value;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    private MockNacosServiceFactory nacosServiceFactory = new MockNacosServiceFactory();

    @Test
    public void testBind() throws NacosException {

        Data data = new Data();

        nacosServiceFactory.setContent("id=1\n name=mercyblitz\nvalue = 0.95");
        nacosServiceFactory.setGroupId(DEFAULT_GROUP);

        ConfigService configService = nacosServiceFactory.createConfigService(null);

        NacosConfigurationPropertiesBinder binder = new NacosConfigurationPropertiesBinder(configService);

        binder.bind(data);

        Assert.assertEquals(1, data.getId());
        Assert.assertEquals("mercyblitz", data.getName());
        Assert.assertTrue(0.95 == data.getValue());
    }

}
