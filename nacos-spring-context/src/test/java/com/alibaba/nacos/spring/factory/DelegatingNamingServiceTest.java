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

package com.alibaba.nacos.spring.factory;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * {@link DelegatingNamingService} Test
 * @author SuperZ1999
 * @date 2023/9/28
 */
public class DelegatingNamingServiceTest {
    @Test
    public void testNamingService() throws NacosException {
        NacosNamingService nacosNamingService = new NacosNamingService("127.0.0.1:8848");
        List<Instance> instances = nacosNamingService.getAllInstances("example");
        System.out.println(instances);
        Assert.assertNotNull(instances);
    }
}