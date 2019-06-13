package com.alibaba.nacos.spring.convert.converter;

import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * @author hylexus
 * Created At 2019-06-13 11:05
 */
public interface NacosPropertySourceConverter {

    /**
     * <pre>
     * (yaml or json or plainText)-based string to properties-based string.
     * </pre>
     * examples:<br>
     * Input (yml-based string):
     * <pre>
     * a:
     *   b: 1
     *   c: 2
     * </pre>
     * Output (properties-based string):
     * <pre>
     *  a.b=1
     *  a.c=2
     * </pre>
     *
     * @param contentFromConfigServer original config from config-server
     * @return converted value
     */
    String convert(String contentFromConfigServer);

    class SimplePropertiesConverter implements NacosPropertySourceConverter {
        @Override
        public String convert(String contentFromConfigServer) {
            return contentFromConfigServer;
        }
    }

    /**
     * A built-in Converter from yaml-based string to properties-based string.
     * Main logic was copied from org.springframework.beans.factory.config.YamlProcessor#buildFlattenedMap(java.util.Map, java.util.Map, java.lang.String)
     * <p>
     * Github Address : https://github.com/spring-projects/spring-framework/blob/5.1.x/spring-beans/src/main/java/org/springframework/beans/factory/config/YamlProcessor.java
     *
     * @author Dave Syer
     * @author Juergen Hoeller
     * @author hylexus
     */
    class SimpleYamlConverter implements NacosPropertySourceConverter {

        @Override
        public String convert(String contentFromConfigServer) {
            Properties properties = yamlStringToProperties(contentFromConfigServer);

            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry entry : properties.entrySet()) {
                stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }

            return stringBuilder.toString();
        }

        private Properties yamlStringToProperties(String yamlString) {
            final Properties result = new Properties();
            final Yaml yaml = new Yaml();
            @SuppressWarnings("unchecked") final Map<String, Map<String, Object>> map = yaml.loadAs(yamlString, Map.class);
            for (Map.Entry<String, Map<String, Object>> e : map.entrySet()) {
                buildFlattenedMap(result, e.getValue(), e.getKey());
            }

            return result;
        }

        /**
         * @author Dave Syer
         * @author Juergen Hoeller
         */
        private void buildFlattenedMap(Properties result, Map<String, Object> source, String path) {
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (StringUtils.hasText(path)) {
                    if (key.startsWith("[")) {
                        key = path + key;
                    } else {
                        key = path + '.' + key;
                    }
                }
                if (value instanceof String) {
                    result.put(key, value);
                } else if (value instanceof Map) {
                    // Need a compound key
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) value;
                    buildFlattenedMap(result, map, key);
                } else if (value instanceof Collection) {
                    // Need a compound key
                    @SuppressWarnings("unchecked")
                    Collection<Object> collection = (Collection<Object>) value;
                    if (collection.isEmpty()) {
                        result.put(key, "");
                    } else {
                        int count = 0;
                        for (Object object : collection) {
                            buildFlattenedMap(result, Collections.singletonMap(
                                    "[" + (count++) + "]", object), key);
                        }
                    }
                } else {
                    result.put(key, (value != null ? value : ""));
                }
            }
        }
    }

}
