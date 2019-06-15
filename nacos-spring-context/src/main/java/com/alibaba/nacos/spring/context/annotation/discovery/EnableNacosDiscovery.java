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
package com.alibaba.nacos.spring.context.annotation.discovery;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.NacosBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

import static com.alibaba.nacos.api.annotation.NacosProperties.*;

/**
 * Annotation for enabling Nacos discovery features.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosBeanDefinitionRegistrar
 * @since 0.1.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NacosDiscoveryBeanDefinitionRegistrar.class)
public @interface EnableNacosDiscovery {

    /**
     * The prefix of property name of Nacos discovery
     */
    String DISCOVERY_PREFIX = NacosProperties.PREFIX + "discovery.";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.discovery.endpoint:${nacos.endpoint:}}"</code>
     */
    String ENDPOINT_PLACEHOLDER = "${" + DISCOVERY_PREFIX + ENDPOINT + ":" + NacosProperties.ENDPOINT_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.discovery.namespace:${nacos.namespace:}}"</code>
     */
    String NAMESPACE_PLACEHOLDER = "${" + DISCOVERY_PREFIX + NAMESPACE + ":" + NacosProperties.NAMESPACE_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.discovery.access-key:${nacos.access-key:}}"</code>
     */
    String ACCESS_KEY_PLACEHOLDER = "${" + DISCOVERY_PREFIX + ACCESS_KEY +":" + NacosProperties.ACCESS_KEY_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.discovery.secret-key:${nacos.secret-key:}}"</code>
     */
    String SECRET_KEY_PLACEHOLDER = "${" + DISCOVERY_PREFIX + SECRET_KEY + ":" + NacosProperties.SECRET_KEY_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.discovery.server-addr:${nacos.server-addr:}}"</code>
     */
    String SERVER_ADDR_PLACEHOLDER = "${" + DISCOVERY_PREFIX + SERVER_ADDR + ":" + NacosProperties.SERVER_ADDR_PLACEHOLDER + "}";


    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.discovery.context-path:${nacos.context-path:}}"</code>
     */
    String CONTEXT_PATH_PLACEHOLDER = "${" + DISCOVERY_PREFIX + CONTEXT_PATH + ":" + NacosProperties.CONTEXT_PATH_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.discovery.cluster-name:${nacos.cluster-name:}}"</code>
     */
    String CLUSTER_NAME_PLACEHOLDER = "${" + DISCOVERY_PREFIX + CLUSTER_NAME + ":" + NacosProperties.CLUSTER_NAME_PLACEHOLDER + "}";

    /**
     * The placeholder of {@link NacosProperties#ENCODE encode}, the value is
     * <code>"${nacos.discovery.encode:${nacos.encode:UTF-8}}"</code>
     */
    String ENCODE_PLACEHOLDER = "${" + DISCOVERY_PREFIX + ENCODE + ":" + NacosProperties.ENCODE_PLACEHOLDER + "}";

    /**
     * Global {@link NacosProperties Nacos Properties}
     *
     * @return required
     * @see NacosInjected#properties()
     */
    NacosProperties globalProperties() default
            @NacosProperties(
                    endpoint = ENDPOINT_PLACEHOLDER,
                    namespace = NAMESPACE_PLACEHOLDER,
                    accessKey = ACCESS_KEY_PLACEHOLDER,
                    secretKey = SECRET_KEY_PLACEHOLDER,
                    serverAddr = SERVER_ADDR_PLACEHOLDER,
                    contextPath = CONTEXT_PATH_PLACEHOLDER,
                    clusterName = CLUSTER_NAME_PLACEHOLDER,
                    encode = ENCODE_PLACEHOLDER
            );
}
