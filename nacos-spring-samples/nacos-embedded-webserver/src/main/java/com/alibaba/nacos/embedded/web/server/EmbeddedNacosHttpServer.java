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
package com.alibaba.nacos.embedded.web.server;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.client.utils.ParamUtil;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Embedded Nacos HTTP Server
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class EmbeddedNacosHttpServer {

    private final HttpServer httpServer;

    private final int port;

    private final String path = "/" + ParamUtil.getDefaultContextPath() + Constants.CONFIG_CONTROLLER_PATH;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Future future;

    private NacosConfigHttpHandler nacosConfigHttpHandler;

    public EmbeddedNacosHttpServer() throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        this.port = httpServer.getAddress().getPort();
        this.nacosConfigHttpHandler = new NacosConfigHttpHandler();
    }

    public EmbeddedNacosHttpServer(int port) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void initConfig(Map<String, String> map) {
        nacosConfigHttpHandler.cacheConfig(map);
    }

    public EmbeddedNacosHttpServer start(boolean blocking) {

        httpServer.createContext(path, nacosConfigHttpHandler);

        nacosConfigHttpHandler.init();

        if (blocking) {
            startServer();
        } else {
            future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    startServer();
                }
            });
        }

        return this;
    }

    private void startServer() {
        httpServer.start();
        String threadName = Thread.currentThread().getName();
        System.out.printf("[%s] Embedded Nacos HTTP Server(port : %d) is starting...%n", threadName, port);
        System.out.printf("[%s] Embedded Nacos HTTP Server mapped request URI : %s...%n", threadName, path);
    }

    public EmbeddedNacosHttpServer stop() {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[%s] Embedded Nacos HTTP Server(port : %d) is stopping...%n", threadName, port);

        if (future != null) {
            if (!future.isDone()) {
                future.cancel(true);
            }
            executorService.shutdown();
        }

        httpServer.stop(0);

        nacosConfigHttpHandler.destroy();

        System.out.printf("[%s] Embedded Nacos HTTP Server(port : %d) is stopped.%n", threadName, port);

        return this;
    }
}
