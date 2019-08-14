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
package com.alibaba.nacos.spring.context.event.config;

import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.config.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Timeout {@link Listener Nacos Config Listener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public abstract class TimeoutNacosConfigListener extends AbstractListener {

    static AtomicInteger id = new AtomicInteger(0);

    static ExecutorService executorService = Executors.newScheduledThreadPool(8, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("com.alibaba.nacos.spring.configListener-" + id.incrementAndGet());
            return t;
        }
    });

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String dataId;

    private final String groupId;

    private final long timeout;

    public TimeoutNacosConfigListener(String dataId, String groupId, long timeout) {
        this.dataId = dataId;
        this.groupId = groupId;
        this.timeout = timeout;
    }

    @Override
    public void receiveConfigInfo(final String content) {
        Future future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                onReceived(content);
            }
        });
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            future.cancel(true);
            if (logger.isWarnEnabled()) {
                logger.warn("Listening on Nacos Config exceeds timeout {} ms " +
                        "[dataId : {}, groupId : {}, data : {}]", timeout, dataId, groupId, content);
            }
        }
    }

    /**
     * process Nacos Config when received.
     *
     * @param content Nacos Config
     */
    protected abstract void onReceived(String content);

    /**
     * Get timeout in milliseconds
     *
     * @return timeout in milliseconds
     */
    public long getTimeout() {
        return timeout;
    }
}
