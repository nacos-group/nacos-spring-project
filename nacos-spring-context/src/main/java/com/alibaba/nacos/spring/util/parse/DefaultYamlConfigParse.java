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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.util.AbstractConfigParse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.parser.ParserException;

/**
 * DefaultYamlConfigParse.
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.0
 */
public class DefaultYamlConfigParse extends AbstractConfigParse {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultYamlConfigParse.class);

	protected static Yaml createYaml() {
		return new Yaml(new MapAppenderConstructor());
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
			LOGGER.debug("Loaded " + count + " document" + (count > 1 ? "s" : "")
					+ " from YAML resource: " + content);
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
			}
			else {
				result.put("[" + key.toString() + "]", value);
			}
		}
		return result;
	}

	protected static Map<String, Object> getFlattenedMap(Map<String, Object> source) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		buildFlattenedMap(result, source, null);
		return result;
	}

	protected static void buildFlattenedMap(Map<String, Object> result,
			Map<String, Object> source, String path) {
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			String key = entry.getKey();
			if (!StringUtils.isBlank(path)) {
				if (key.startsWith("[")) {
					key = path + key;
				}
				else {
					key = path + '.' + key;
				}
			}
			Object value = entry.getValue();
			if (value instanceof String) {
				result.put(key, value);
			}
			else if (value instanceof Map) {
				// Need a compound key
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) value;
				buildFlattenedMap(result, map, key);
			}
			else if (value instanceof Collection) {
				// Need a compound key
				@SuppressWarnings("unchecked")
				Collection<Object> collection = (Collection<Object>) value;
				int count = 0;
				for (Object object : collection) {
					buildFlattenedMap(result,
							Collections.singletonMap("[" + (count++) + "]", object), key);
				}
			}
			else {
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

	protected static class MapAppenderConstructor extends Constructor {

		MapAppenderConstructor() {
			super();
		}

		@Override
		protected Map<Object, Object> constructMapping(MappingNode node) {
			try {
				return super.constructMapping(node);
			}
			catch (IllegalStateException ex) {
				throw new ParserException("while parsing MappingNode",
						node.getStartMark(), ex.getMessage(), node.getEndMark());
			}
		}

		@Override
		protected Map<Object, Object> createDefaultMap() {
			final Map<Object, Object> delegate = super.createDefaultMap();
			return new AbstractMap<Object, Object>() {
				@Override
				public Object put(Object key, Object value) {
					if (delegate.containsKey(key)) {
						throw new IllegalStateException("Duplicate key: " + key);
					}
					return delegate.put(key, value);
				}

				@Override
				public Set<Entry<Object, Object>> entrySet() {
					return delegate.entrySet();
				}
			};
		}
	}

}
