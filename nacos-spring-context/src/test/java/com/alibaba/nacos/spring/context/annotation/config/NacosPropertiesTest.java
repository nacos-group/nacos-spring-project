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
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * {@link NacosProperties} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosProperties
 * @since 0.1.0
 */
public class NacosPropertiesTest {

    @EnableNacos(globalProperties = @NacosProperties)
    private static class NacosPropertiesDefaultValues {
    }

    @EnableNacos(
            globalProperties =
            @NacosProperties(
                    endpoint = "e",
                    namespace = "n",
                    accessKey = "a",
                    secretKey = "s",
                    serverAddr = "127.0.0.1",
                    contextPath = "/",
                    clusterName = "c",
                    encode = "GBK"
            )
    )
    private static class NacosPropertiesValues {
    }

    @Test
    public void testConstants() {
        Assert.assertEquals("nacos.", NacosProperties.PREFIX);
        Assert.assertEquals("endpoint", NacosProperties.ENDPOINT);
        Assert.assertEquals("namespace", NacosProperties.NAMESPACE);
        Assert.assertEquals("access-key", NacosProperties.ACCESS_KEY);
        Assert.assertEquals("secret-key", NacosProperties.SECRET_KEY);
        Assert.assertEquals("server-addr", NacosProperties.SERVER_ADDR);
        Assert.assertEquals("context-path", NacosProperties.CONTEXT_PATH);
        Assert.assertEquals("cluster-name", NacosProperties.CLUSTER_NAME);
        Assert.assertEquals("encode", NacosProperties.ENCODE);
    }

    @Test
    public void testAttributeDefaultValues() {
        NacosProperties nacosProperties = getDefaultNacosProperties();
        Assert.assertEquals("${nacos.endpoint:}", nacosProperties.endpoint());
        Assert.assertEquals("${nacos.namespace:}", nacosProperties.namespace());
        Assert.assertEquals("${nacos.access-key:}", nacosProperties.accessKey());
        Assert.assertEquals("${nacos.secret-key:}", nacosProperties.secretKey());
        Assert.assertEquals("${nacos.server-addr:}", nacosProperties.serverAddr());
        Assert.assertEquals("${nacos.context-path:}", nacosProperties.contextPath());
        Assert.assertEquals("${nacos.cluster-name:}", nacosProperties.clusterName());
        Assert.assertEquals("${nacos.encode:UTF-8}", nacosProperties.encode());
    }

    @Test
    public void testAttributeValues() {
        EnableNacos enableNacos = NacosPropertiesValues.class.getAnnotation(EnableNacos.class);
        NacosProperties nacosProperties = enableNacos.globalProperties();
        Assert.assertEquals("e", nacosProperties.endpoint());
        Assert.assertEquals("n", nacosProperties.namespace());
        Assert.assertEquals("a", nacosProperties.accessKey());
        Assert.assertEquals("s", nacosProperties.secretKey());
        Assert.assertEquals("127.0.0.1", nacosProperties.serverAddr());
        Assert.assertEquals("/", nacosProperties.contextPath());
        Assert.assertEquals("c", nacosProperties.clusterName());
        Assert.assertEquals("GBK", nacosProperties.encode());
    }

    @Test
    public void testAttributeResolvedDefaultValues() {
        NacosProperties nacosProperties = getDefaultNacosProperties();
        MockEnvironment environment = new MockEnvironment();
        Assert.assertEquals("", environment.resolvePlaceholders(nacosProperties.endpoint()));
        Assert.assertEquals("", environment.resolvePlaceholders(nacosProperties.namespace()));
        Assert.assertEquals("", environment.resolvePlaceholders(nacosProperties.accessKey()));
        Assert.assertEquals("", environment.resolvePlaceholders(nacosProperties.secretKey()));
        Assert.assertEquals("", environment.resolvePlaceholders(nacosProperties.serverAddr()));
        Assert.assertEquals("", environment.resolvePlaceholders(nacosProperties.contextPath()));
        Assert.assertEquals("", environment.resolvePlaceholders(nacosProperties.clusterName()));
        Assert.assertEquals("UTF-8", environment.resolvePlaceholders(nacosProperties.encode()));
    }

    @Test
    public void testAttributeResolvedPropertyValues() {
        NacosProperties nacosProperties = getDefaultNacosProperties();
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("nacos.endpoint", "e");
        environment.setProperty("nacos.namespace", "n");
        environment.setProperty("nacos.access-key", "a");
        environment.setProperty("nacos.secret-key", "s");
        environment.setProperty("nacos.server-addr", "127.0.0.1");
        environment.setProperty("nacos.context-path", "/");
        environment.setProperty("nacos.cluster-name", "c");
        environment.setProperty("nacos.encode", "GBK");

        Assert.assertEquals("e", environment.resolvePlaceholders(nacosProperties.endpoint()));
        Assert.assertEquals("n", environment.resolvePlaceholders(nacosProperties.namespace()));
        Assert.assertEquals("a", environment.resolvePlaceholders(nacosProperties.accessKey()));
        Assert.assertEquals("s", environment.resolvePlaceholders(nacosProperties.secretKey()));
        Assert.assertEquals("127.0.0.1", environment.resolvePlaceholders(nacosProperties.serverAddr()));
        Assert.assertEquals("/", environment.resolvePlaceholders(nacosProperties.contextPath()));
        Assert.assertEquals("c", environment.resolvePlaceholders(nacosProperties.clusterName()));
        Assert.assertEquals("GBK", environment.resolvePlaceholders(nacosProperties.encode()));
    }

    private NacosProperties getDefaultNacosProperties() {
        EnableNacos enableNacos = NacosPropertiesDefaultValues.class.getAnnotation(EnableNacos.class);
        NacosProperties nacosProperties = enableNacos.globalProperties();
        return nacosProperties;
    }

}
