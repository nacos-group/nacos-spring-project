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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * {@link PropertiesPlaceholderResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class PropertiesPlaceholderResolverTest {

    @Test
    public void testResolve() {

        MockEnvironment environment = new MockEnvironment();

        PropertiesPlaceholderResolver resolver = new PropertiesPlaceholderResolver(environment);

        Map properties = new HashMap();
        properties.put("my.name", "${my.name}");
        properties.put("my.age", 18);

        environment.setProperty("my.name", "mercyblitz");
        environment.setProperty("my.age", "18");

        Properties resolvedProperties = resolver.resolve(properties);

        Assert.assertEquals(resolvedProperties.get("my.name"), "mercyblitz");
        Assert.assertNull(resolvedProperties.get("my.age"));


    }

}
