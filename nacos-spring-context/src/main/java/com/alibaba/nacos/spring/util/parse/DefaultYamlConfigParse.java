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

package com.alibaba.nacos.spring.util.parse;

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.util.AbstractConfigParse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * DefaultYamlConfigParse.
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.0
 */
public class DefaultYamlConfigParse extends AbstractConfigParse {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultYamlConfigParse.class);

    private static final String YAML_ALLOW_COMPLEX_OBJECT = "yamlAllowComplexObject";

    private static boolean getYamlAllowComplexObject() {
        return Boolean.getBoolean(YAML_ALLOW_COMPLEX_OBJECT);
    }

    protected static Yaml createYaml() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        SafeConstructor constructor;
        if (getYamlAllowComplexObject()) {
            constructor = new Constructor(loaderOptions);
        } else {
            constructor = new SafeConstructor(loaderOptions);
        }
        DumperOptions dumperOptions = new DumperOptions();
        Representer representer = new Representer(dumperOptions);
        LimitedResolver resolver = new LimitedResolver();
        return new Yaml(constructor, representer, dumperOptions, loaderOptions, resolver);
    }

    protected static boolean process(MatchCallback callback, Yaml yaml, String content) {
        int count = 0;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Loading from YAML: " + content);
        }
        for (Object object : yaml.loadAll(content)) {
            if (object != null && process(asMap(object), callback)) {
                count++;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Loaded " + count + " document" + (count > 1 ? "s" : "") + " from YAML resource: " + content);
        }
        return (count > 0);
    }

    protected static boolean process(Map<String, Object> map, MatchCallback callback) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Merging document (no matchers set): " + map);
        }
        callback.process(getFlattenedMap(map));
        return true;
    }

    @SuppressWarnings("unchecked")
    protected static Map<String, Object> asMap(Object object) {
        // YAML can have numbers as keys
        Map<String, Object> result = new LinkedHashMap();
        if (!(object instanceof Map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = asMap(value);
            }
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                result.put("[" + key.toString() + "]", value);
            }
        }
        return result;
    }

    private static class LimitedResolver extends Resolver {

        @Override
        public void addImplicitResolver(Tag tag, Pattern regexp, String first) {
            if (tag == Tag.TIMESTAMP) {
                return;
            }
            super.addImplicitResolver(tag, regexp, first);
        }
    }

    protected static Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    protected static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isBlank(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked") Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
                }
            } else {
                result.put(key, (value != null ? value.toString() : ""));
            }
        }
    }

    @Override
    public Map<String, Object> parse(String configText) {
        final AtomicReference<Map<String, Object>> result = new AtomicReference<Map<String, Object>>();
        process(new MatchCallback() {
            @Override
            public void process(Map<String, Object> map) {
                result.set(map);
            }
        }, createYaml(), configText);
        return result.get();
    }

    @Override
    public String processType() {
        return ConfigType.YAML.getType();
    }

    protected interface MatchCallback {

        /**
         * Put Map to Properties.
         *
         * @param map {@link Map}
         */
        void process(Map<String, Object> map);
    }

}
