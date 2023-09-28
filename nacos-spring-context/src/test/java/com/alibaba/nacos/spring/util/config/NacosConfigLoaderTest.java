/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.alibaba.nacos.spring.util.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * {@link NacosConfigLoader} Test
 * @author SuperZ1999
 * @date 2023/9/28
 */
public class NacosConfigLoaderTest {
    @Test
    public void testNacosConfigLoader() {
        MockEnvironment environment = new MockEnvironment();
        NacosConfigLoader nacosConfigLoader = new NacosConfigLoader(environment);
        Integer convert = environment.getConversionService().convert("12", Integer.class);
        Assert.assertEquals(Integer.valueOf(12), convert);
    }
}