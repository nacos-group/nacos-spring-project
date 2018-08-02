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

import com.alibaba.nacos.api.config.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Non Blocking {@link Listener Nacos Config Listener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public abstract class NonBlockingNacosConfigListener implements Listener {

    private final String dataId;

    private final String groupId;

    private final ExecutorService nacosConfigListenerExecutor;

    private final long timeout;

    /**
     * Executor used for timeout
     */
    private final ExecutorService executor;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public NonBlockingNacosConfigListener(String dataId, String groupId, ExecutorService nacosConfigListenerExecutor,
                                          long timeout) {
        this.dataId = dataId;
        this.groupId = groupId;
        this.nacosConfigListenerExecutor = nacosConfigListenerExecutor;
        this.timeout = timeout;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public Executor getExecutor() {
        return nacosConfigListenerExecutor;
    }

    @Override
    public void receiveConfigInfo(final String configInfo) {

        Future future = executor.submit(new Runnable() {
            @Override
            public void run() {
                onUpdate(configInfo);
            }
        });

        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Listening on Nacos Config exceeds timeout %d ms [dataId : %s, groupId : %s, data : %s]"
                        , timeout, dataId, groupId, configInfo));
            }
        }

    }

    /**
     * On config update
     *
     * @param config config
     */
    protected abstract void onUpdate(String config);
}
