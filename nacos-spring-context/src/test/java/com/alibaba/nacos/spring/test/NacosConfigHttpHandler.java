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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.Charset.forName;
import static org.springframework.util.StreamUtils.copyToString;
import static org.springframework.util.StringUtils.trimAllWhitespace;

/**
 * Nacos Config {@link HttpHandler} which only supports request parameters :
 * <ul>
 * <li>{@link #DATA_ID_PARAM_NAME}</li>
 * <li>{@link #GROUP_ID_PARAM_NAME}</li>
 * <li>{@link #CONTENT_PARAM_NAME}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
class NacosConfigHttpHandler implements HttpHandler {

    private Map<String, String> contentCache = new HashMap<String, String>();

    private static final String DATA_ID_PARAM_NAME = "dataId";

    private static final String GROUP_ID_PARAM_NAME = "group";

    private static final String CONTENT_PARAM_NAME = "content";

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        if ("GET".equals(method)) {
            handleGetConfig(httpExchange);
        } else if ("POST".equals(method)) {
            handlePublishConfig(httpExchange);
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
    protected void handlePublishConfig(HttpExchange httpExchange) throws IOException {

        String queryString = copyToString(httpExchange.getRequestBody(), forName("UTF-8"));

        Map<String, String> params = parseParams(queryString);

        String content = params.get(CONTENT_PARAM_NAME);

        String key = createContentKey(params);

        contentCache.put(key, content);

        write(httpExchange, "true");

    }

    /**
     * Handle {@link ConfigService#getConfig(String, String, long)}
     *
     * @param httpExchange {@link HttpExchange}
     * @throws IOException IO error
     */
    protected void handleGetConfig(HttpExchange httpExchange) throws IOException {

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
        return dataId + " | " + groupId;
    }

    private void write(HttpExchange httpExchange, String content) throws IOException {
        if (StringUtils.hasText(content)) {
            OutputStream outputStream = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(200, content.length());
            StreamUtils.copy(content, forName("UTF-8"), outputStream);
        }
        httpExchange.close();
    }

    private Map<String, String> parseParams(String queryString) {
        Map<String, String> params = new HashMap<String, String>();
        String[] parts = StringUtils.delimitedListToStringArray(queryString, "&");
        for (String part : parts) {
            String[] nameAndValue = StringUtils.split(part, "=");
            params.put(trimAllWhitespace(nameAndValue[0]), trimAllWhitespace(nameAndValue[1]));
        }
        return params;
    }
}
