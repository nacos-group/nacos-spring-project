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

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Mock {@link ConfigService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class MockConfigService implements ConfigService {

    private Map<String, List<Listener>> listenersCache = new LinkedHashMap<String, List<Listener>>();

    private Map<String, String> contentCache = new LinkedHashMap<String, String>();

    public static final String TIMEOUT_ERROR_MESSAGE = "Timeout must not be less then zero.";

    @Override
    public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
        String key = createKey(dataId, group);
        if (timeoutMs < 0) {
            throw new NacosException(NacosException.SERVER_ERROR, TIMEOUT_ERROR_MESSAGE);
        }
        return contentCache.get(key);
    }

    @Override
    public String getConfigAndSignListener(String dataId, String group, long timeoutMs, Listener listener) throws NacosException {
        return null;
    }

    @Override
    public void addListener(String dataId, String group, Listener listener) throws NacosException {
        String key = createKey(dataId, group);
        List<Listener> listeners = listenersCache.get(key);
        if (listeners == null) {
            listeners = new LinkedList<Listener>();
            listenersCache.put(key, listeners);
        }
        listeners.add(listener);
    }

    @Override
    public boolean publishConfig(String dataId, String group, final String content) throws NacosException {
        String key = createKey(dataId, group);
        contentCache.put(key, content);

        List<Listener> listeners = listenersCache.get(key);
        if (!CollectionUtils.isEmpty(listeners)) {
            for (final Listener listener : listeners) {
                Executor executor = listener.getExecutor();
                if (executor != null) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.receiveConfigInfo(content);
                        }
                    });
                } else {
                    listener.receiveConfigInfo(content);
                }
            }
        }

        return true;
    }

    @Override
    public boolean removeConfig(String dataId, String group) throws NacosException {
        String key = createKey(dataId, group);
        return contentCache.remove(key) != null;
    }

    @Override
    public void removeListener(String dataId, String group, Listener listener) {
        String key = createKey(dataId, group);
        listenersCache.remove(key);
    }

    @Override
    public String getServerStatus() {
        return "UP";
    }

    private String createKey(String dataId, String groupId) {
        return dataId + "&" + groupId;
    }
}
