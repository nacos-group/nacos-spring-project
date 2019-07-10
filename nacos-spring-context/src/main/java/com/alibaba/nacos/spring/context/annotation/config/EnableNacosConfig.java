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

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.spring.context.annotation.NacosBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

import static com.alibaba.nacos.api.annotation.NacosProperties.*;

/**
 * Annotation for enabling Nacos Config features.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosBeanDefinitionRegistrar
 * @since 0.1.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NacosConfigBeanDefinitionRegistrar.class)
public @interface EnableNacosConfig {

    /**
     * The prefix of property name of Nacos Config
     */
    String CONFIG_PREFIX = NacosProperties.PREFIX + "config.";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.config.endpoint:${nacos.endpoint:}}"</code>
     */
    String ENDPOINT_PLACEHOLDER = "${" + CONFIG_PREFIX + ENDPOINT + ":" + NacosProperties.ENDPOINT_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.config.namespace:${nacos.namespace:}}"</code>
     */
    String NAMESPACE_PLACEHOLDER = "${" + CONFIG_PREFIX + NAMESPACE + ":" + NacosProperties.NAMESPACE_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.config.access-key:${nacos.access-key:}}"</code>
     */
    String ACCESS_KEY_PLACEHOLDER = "${" + CONFIG_PREFIX + ACCESS_KEY + ":" + NacosProperties.ACCESS_KEY_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.config.secret-key:${nacos.secret-key:}}"</code>
     */
    String SECRET_KEY_PLACEHOLDER = "${" + CONFIG_PREFIX + SECRET_KEY + ":" + NacosProperties.SECRET_KEY_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.config.server-addr:${nacos.server-addr:}}"</code>
     */
    String SERVER_ADDR_PLACEHOLDER = "${" + CONFIG_PREFIX + SERVER_ADDR + ":" + NacosProperties.SERVER_ADDR_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.config.context-path:${nacos.context-path:}}"</code>
     */
    String CONTEXT_PATH_PLACEHOLDER = "${" + CONFIG_PREFIX + CONTEXT_PATH + ":" + NacosProperties.CONTEXT_PATH_PLACEHOLDER + "}";

    /**
     * The placeholder of endpoint, the value is
     * <code>"${nacos.config.cluster-name:${nacos.cluster-name:}}"</code>
     */
    String CLUSTER_NAME_PLACEHOLDER = "${" + CONFIG_PREFIX + CLUSTER_NAME + ":" + NacosProperties.CLUSTER_NAME_PLACEHOLDER + "}";

    /**
     * The placeholder of {@link NacosProperties#ENCODE encode}, the value is
     * <code>"${nacos.config.encode:${nacos.encode:UTF-8}}"</code>
     */
    String ENCODE_PLACEHOLDER = "${" + CONFIG_PREFIX + ENCODE + ":" + NacosProperties.ENCODE_PLACEHOLDER + "}";

    /**
     * The placeholder of {@link NacosProperties#CONFIG_LONG_POLL_TIMEOUT configLongPollTimeout},
     * the value is <code>"${nacos.configLongPollTimeout:}"</code>
     */
    String CONFIG_LONG_POLL_TIMEOUT_PLACEHOLDER = "${" + CONFIG_PREFIX + PREFIX + NacosProperties.CONFIG_LONG_POLL_TIMEOUT_PLACEHOLDER + "}";

    /**
     * The placeholder of {@link NacosProperties#CONFIG_RETRY_TIME configRetryTime},
     * the value is <code>"${nacos.configRetryTime:}"</code>
     */
    String CONFIG_RETRY_TIME_PLACEHOLDER = "${" + CONFIG_PREFIX + PREFIX + NacosProperties.CONFIG_RETRY_TIME_PLACEHOLDER + "}";

    /**
     * The placeholder of {@link NacosProperties#MAX_RETRY maxRetry}, the value is <code>"${nacos.maxRetry:}"</code>
     */
    String MAX_RETRY_PLACEHOLDER = "${" + CONFIG_PREFIX + PREFIX + NacosProperties.MAX_RETRY_PLACEHOLDER + "}";

    /**
     * The placeholder of {@link NacosProperties#ENABLE_REMOTE_SYNC_CONFIG_PLACEHOLDER enableRemoteSyncConfig}
     * the value is <code>"${nacos.enableRemoteSyncConfig:}"</code>
     */
//    String ENABLE_REMOTE_SYNC_CONFIG_PLACEHOLDER = "${" + CONFIG_PREFIX + ENCODE + ":" + NacosProperties.ENABLE_REMOTE_SYNC_CONFIG_PLACEHOLDER + "}";

    /**
     * Global {@link NacosProperties Nacos Properties}
     *
     * @return required
     * @see NacosInjected#properties()
     * @see NacosConfigListener#properties()
     * @see NacosConfigurationProperties#properties()
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
                    encode = ENCODE_PLACEHOLDER,
                    configLongPollTimeout = CONFIG_LONG_POLL_TIMEOUT_PLACEHOLDER,
                    configRetryTime = CONFIG_RETRY_TIME_PLACEHOLDER,
                    maxRetry = MAX_RETRY_PLACEHOLDER
//                    enableRemoteSyncConfig = ENABLE_REMOTE_SYNC_CONFIG_PLACEHOLDER
            );
}
