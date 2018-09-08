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
package com.alibaba.nacos.spring.util;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * {@link NacosUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosUtilsTest {

    @NacosInjected
    private Object object = new Object();

    @NacosInjected(properties = @NacosProperties(serverAddr = "test"))
    private Object object2 = new Object();

    @Test
    public void testIsDefault() {

        testIsDefault("object", true);
        testIsDefault("object2", false);
    }

    private void testIsDefault(String fieldName, boolean expectedValue) {

        Field objectField = ReflectionUtils.findField(getClass(), fieldName);

        NacosInjected nacosInjected = objectField.getAnnotation(NacosInjected.class);

        NacosProperties nacosProperties = nacosInjected.properties();

        Assert.assertEquals(expectedValue, NacosUtils.isDefault(nacosProperties));

    }
}
