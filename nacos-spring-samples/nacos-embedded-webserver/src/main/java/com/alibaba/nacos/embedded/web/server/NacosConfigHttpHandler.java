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
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.common.util.Md5Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.Charset.forName;

/**
 * Nacos Config {@link HttpHandler} which only supports request parameters :
 * <ul>
 * <li>{@link #DATA_ID_PARAM_NAME}</li>
 * <li>{@link #GROUP_ID_PARAM_NAME}</li>
 * <li>{@link #CONTENT_PARAM_NAME}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public class NacosConfigHttpHandler implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, String> contentCache = new HashMap<String, String>();

    private Map<String, LongPolling> longPollingMap = new HashMap<String, LongPolling>();

    private ScheduledExecutorService scheduledExecutorService;

    private volatile boolean isRunning;

    public static final String DATA_ID_PARAM_NAME = "dataId";

    public static final String GROUP_ID_PARAM_NAME = "group";

    public static final String CONTENT_PARAM_NAME = "content";

    private static final Object LOCK = new Object();

    public void init() {
        isRunning = true;
        final int maxWaitInSecond = 3;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    synchronized (LOCK) {
                        Set<String> keySet = new HashSet<String>(longPollingMap.keySet());
                        for (String contentKey : keySet) {
                            LongPolling longPolling = longPollingMap.get(contentKey);
                            if (longPolling == null) {
                                continue;
                            }
                            if (System.currentTimeMillis() - longPolling.date.getTime() > (maxWaitInSecond * 1000L)) {
                                try {
                                    write(longPolling.httpExchange, "");
                                } catch (IOException e) {
                                    logger.error("Polling task encountered an exception, contentKey: " + contentKey, e);
                                }
                                removeLongPolling(longPolling.httpExchange);
                            }
                        }
                    }
                }
            }
        }, maxWaitInSecond, maxWaitInSecond, TimeUnit.SECONDS);
    }


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        if ("GET".equals(method)) {
            handleGetConfig(httpExchange);
        } else if ("POST".equals(method)) {
            String queryString = StreamUtils.copyToString(httpExchange.getRequestBody(), forName("UTF-8"));
            Map<String, String> params = parseParams(queryString);
            String listeningConfigs = params.get("Listening-Configs");
            if (listeningConfigs != null) {
                handleLongPolling(httpExchange, listeningConfigs);
            } else {
                handlePublishConfig(httpExchange, params);
            }
        } else if ("DELETE".equals(method)) {
            handleRemoveConfig(httpExchange);
        }
    }

    /**
     * Handle {@link ConfigService#publishConfig(String, String, String)}
     *
     * @param httpExchange {@link HttpExchange}
     * @throws IOException IO error
     */
    private void handlePublishConfig(HttpExchange httpExchange,  Map<String, String> params) throws IOException {
        cacheConfig(params);

        notifyLongPolling(params);

        write(httpExchange, "true");
    }

    private void notifyLongPolling(Map<String, String> params) throws IOException {
        String contentKey = createContentKey(params);
        synchronized (LOCK) {
            LongPolling longPolling = longPollingMap.get(contentKey);
            if (longPolling != null) {
                String dataId = params.get(DATA_ID_PARAM_NAME);
                String groupId = params.get(GROUP_ID_PARAM_NAME);
                String longPollingResult = createLongPollingResult(dataId, groupId);
                removeLongPolling(longPolling.httpExchange);
                write(longPolling.httpExchange, longPollingResult);
            }
        }
    }

    private void removeLongPolling(HttpExchange httpExchange) {
        Set<String> keySet = new HashSet<String>(longPollingMap.keySet());
        for (String key : keySet) {
            LongPolling longPolling = longPollingMap.get(key);
            if (longPolling == null) {
                continue;
            }
            if (longPolling.httpExchange.equals(httpExchange)) {
                longPollingMap.remove(key);
            }
        }
    }

    private String createLongPollingResult(String dataId, String groupId) throws IOException {
        String sb = dataId + Constants.WORD_SEPARATOR + groupId + Constants.LINE_SEPARATOR;
        return URLEncoder.encode(sb, "UTF-8");
    }

    private void handleLongPolling(HttpExchange httpExchange, String listeningConfigs) throws IOException {
        // @see ClientWorker.checkUpdateDataIds
        listeningConfigs = URLDecoder.decode(listeningConfigs, "UTF-8");

        List<String> changeDataIdList = new ArrayList<String>();
        List<String> changeGroupIdList = new ArrayList<String>();
        List<String> contentKeyList = new ArrayList<String>();

        String[] lines = listeningConfigs.split(Constants.LINE_SEPARATOR);
        for (String line : lines) {
            parseLine(changeDataIdList, changeGroupIdList, contentKeyList, line);
        }

        if (!changeDataIdList.isEmpty()) {
            String longPollingResult = createLongPollingResult(changeDataIdList, changeGroupIdList);
            write(httpExchange, longPollingResult);
            return;
        }

        synchronized (LOCK) {
            for (String contentKey : contentKeyList) {
                longPollingMap.put(contentKey, new LongPolling(httpExchange));
            }
        }
    }

    private void parseLine(List<String> changeDataIdList, List<String> changeGroupIdList, List<String> contentKeyList,
                           String line) {
        String[] arr = line.split(Constants.WORD_SEPARATOR, 3);
        if (arr.length < 3) {
            logger.warn("Listening-Configs is wrong format, line: {}", line);
            return;
        }
        String dataId = arr[0];
        String groupId = arr[1];
        String md5 = arr[2];
        String contentKey = createContentKey(dataId, groupId);
        String content = contentCache.get(contentKey);
        if (content != null) {
            if (!md5.equals(Md5Utils.getMD5(content, "UTF-8"))) {
                changeDataIdList.add(dataId);
                changeGroupIdList.add(groupId);
                return;
            }
        }
        contentKeyList.add(contentKey);
    }

    private String createLongPollingResult(List<String> dataIdList, List<String> groupIdList) throws IOException {
        StringBuilder sb = new StringBuilder();
        int size = dataIdList.size();
        for (int i = 0; i < size; i++) {
            sb.append(dataIdList.get(i));
            sb.append(Constants.WORD_SEPARATOR);
            sb.append(groupIdList.get(i));
            sb.append(Constants.LINE_SEPARATOR);
        }
        return URLEncoder.encode(sb.toString(), "UTF-8");
    }

    public void cacheConfig(Map<String, String> params) {
        String content = params.get(CONTENT_PARAM_NAME);

        String key = createContentKey(params);

        contentCache.put(key, content);
    }

    /**
     * Handle {@link ConfigService#getConfig(String, String, long)}
     *
     * @param httpExchange {@link HttpExchange}
     * @throws IOException IO error
     */
    private void handleGetConfig(HttpExchange httpExchange) throws IOException {

        URI requestURI = httpExchange.getRequestURI();

        String queryString = requestURI.getQuery();

        Map<String, String> params = parseParams(queryString);

        String key = createContentKey(params);

        String content = contentCache.get(key);

        write(httpExchange, content);
    }

    /**
     * Handle {@link ConfigService#removeConfig(String, String)}
     *
     * @param httpExchange {@link HttpExchange}
     * @throws IOException IO error
     */
    private void handleRemoveConfig(HttpExchange httpExchange) throws IOException {

        URI requestURI = httpExchange.getRequestURI();

        String queryString = requestURI.getQuery();

        Map<String, String> params = parseParams(queryString);

        String key = createContentKey(params);

        contentCache.remove(key);

        write(httpExchange, "OK");
    }

    private String createContentKey(Map<String, String> params) {
        String dataId = params.get(DATA_ID_PARAM_NAME);
        String groupId = params.get(GROUP_ID_PARAM_NAME);
        return createContentKey(dataId, groupId);
    }

    private String createContentKey(String dataId, String groupId) {
        return dataId + " | " + groupId;
    }

    private void write(HttpExchange httpExchange, String content) throws IOException {
        if (content != null) {
            OutputStream outputStream = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(200, content.length());
            StreamUtils.copy(URLDecoder.decode(content, "UTF-8"), forName("UTF-8"), outputStream);
        }
        httpExchange.close();
    }

    private Map<String, String> parseParams(String queryString) {
        Map<String, String> params = new HashMap<String, String>();
        String[] parts = StringUtils.delimitedListToStringArray(queryString, "&");
        for (String part : parts) {
            String[] nameAndValue = StringUtils.split(part, "=");
            params.put(StringUtils.trimAllWhitespace(nameAndValue[0]), StringUtils.trimAllWhitespace(nameAndValue[1]));
        }
        return params;
    }

    public void destroy() {
        isRunning = false;
        if(scheduledExecutorService != null){
            scheduledExecutorService.shutdown();
        }
    }

    private static class LongPolling {
        private HttpExchange httpExchange;
        private Date date;

        LongPolling(HttpExchange httpExchange) {
            this.httpExchange = httpExchange;
            this.date = new Date();
        }
    }
}
