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

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.CONTEXT_PATH;
import static com.alibaba.nacos.api.PropertyKeyConst.ENCODE;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.util.StringUtils.hasText;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.config.annotation.NacosIgnore;
import com.alibaba.nacos.api.config.annotation.NacosProperty;
import com.alibaba.nacos.api.exception.NacosException;

/**
 * Nacos Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public abstract class NacosUtils {

	/**
	 * Default value of {@link String} attribute for {@link Annotation}
	 */
	public static final String DEFAULT_STRING_ATTRIBUTE_VALUE = "";

	/**
	 * Default value of {@link String} attribute for {@link Annotation}
	 */
	public static final String DEFAULT_CONFIG_TYPE_VALUE = "properties";

	/**
	 * Default value of boolean attribute for {@link Annotation}
	 */
	public static final boolean DEFAULT_BOOLEAN_ATTRIBUTE_VALUE = false;

	/**
	 * The separator
	 */
	public static final String SEPARATOR = "|";

	/**
	 * Default timeout for getting Nacos configuration
	 */
	public static final long DEFAULT_TIMEOUT = Long.getLong("nacos.default.timeout",
			5000L);

	private static final Set<Class<?>> NON_BEAN_CLASSES = Collections.unmodifiableSet(
			new HashSet<Class<?>>(Arrays.asList(Object.class, Class.class)));

	private static final Logger logger = LoggerFactory.getLogger(NacosUtils.class);

	private static Boolean readTypeFromDataId = null;

	public static Boolean isReadTypeFromDataId() {
		return readTypeFromDataId;
	}

	public static void setReadTypeFromDataIdIfNull(boolean readTypeFromDataId) {
		if (NacosUtils.readTypeFromDataId == null) {
			NacosUtils.readTypeFromDataId = readTypeFromDataId;
		}
	}

	public static void resetReadTypeFromDataId() {
		NacosUtils.readTypeFromDataId = null;
	}

	/**
	 * Build The default name of {@link NacosConfigurationProperties @NacosPropertySource}
	 *
	 * @param dataId data Id
	 * @param groupId group Id
	 * @param properties Nacos Properties
	 * @return non-null
	 */
	public static String buildDefaultPropertySourceName(String dataId, String groupId,
			Map<?, ?> properties) {
		return build(dataId, groupId, identify(properties));
	}

	/**
	 * Generate Id of {@link NacosProperties Nacos Properties annotation}
	 *
	 * @param nacosProperties {@link NacosProperties Nacos Properties annotation}
	 * @return Id
	 */
	public static String identify(NacosProperties nacosProperties) {
		return identify(getAnnotationAttributes(nacosProperties));
	}

	/**
	 * Generate Id of {@link NacosProperties Nacos Properties}
	 *
	 * @param properties {@link Properties Nacos Properties}
	 * @return Id
	 */

	public static String identify(Map<?, ?> properties) {

		String namespace = (String) properties.get(NAMESPACE);
		String serverAddress = (String) properties.get(SERVER_ADDR);
		String contextPath = (String) properties.get(CONTEXT_PATH);
		String clusterName = (String) properties.get(CLUSTER_NAME);
		String endpoint = (String) properties.get(ENDPOINT);
		String accessKey = (String) properties.get(ACCESS_KEY);
		String secretKey = (String) properties.get(SECRET_KEY);
		String encode = (String) properties.get(ENCODE);

		return build(namespace, clusterName, serverAddress, contextPath, endpoint,
				accessKey, secretKey, encode);

	}

	private static String build(Object... values) {
		StringBuilder stringBuilder = new StringBuilder();

		for (Object value : values) {

			String stringValue = value == null ? null : String.valueOf(value);
			if (StringUtils.hasText(stringValue)) {
				stringBuilder.append(stringValue);
			}
			stringBuilder.append(SEPARATOR);
		}

		return stringBuilder.toString();
	}

	/**
	 * Is {@link NacosProperties @NacosProperties} with default attribute values.
	 *
	 * @param nacosProperties {@link NacosProperties @NacosProperties}
	 * @return If default values , return <code>true</code>,or <code>false</code>
	 */
	public static boolean isDefault(final NacosProperties nacosProperties) {

		final List<Object> records = new LinkedList<Object>();

		ReflectionUtils.doWithMethods(nacosProperties.annotationType(),
				new ReflectionUtils.MethodCallback() {
					@Override
					public void doWith(Method method)
							throws IllegalArgumentException, IllegalAccessException {
						if (Modifier.isPublic(method.getModifiers())
								&& method.getParameterTypes().length == 0) {
							Object defaultValue = method.getDefaultValue();
							if (defaultValue != null) {
								try {
									Object returnValue = method.invoke(nacosProperties);
									if (!defaultValue.equals(returnValue)) {
										records.add(returnValue);
									}
								}
								catch (Exception e) {
								}
							}
						}
					}
				});

		return records.isEmpty();
	}

	public static String readFromEnvironment(String label, Environment environment) {
		return environment.resolvePlaceholders(label);
	}

	public static String readFileExtension(String dataId) {
		int lastIndex = dataId.lastIndexOf(".");
		return dataId.substring(lastIndex + 1);
	}

	public static PropertyValues resolvePropertyValues(Object bean, String content,
			String type) {
		return resolvePropertyValues(bean, "", "", "", content, type);
	}

	public static PropertyValues resolvePropertyValues(Object bean, final String prefix,
			String dataId, String groupId, String content, String type) {
		final Map<String, Object> configProperties = toProperties(dataId, groupId,
				content, type);
		final MutablePropertyValues propertyValues = new MutablePropertyValues();
		ReflectionUtils.doWithFields(bean.getClass(),
				new ReflectionUtils.FieldCallback() {
					@Override
					public void doWith(Field field)
							throws IllegalArgumentException, IllegalAccessException {
						String propertyName = NacosUtils.resolvePropertyName(field);
						propertyName = StringUtils.isEmpty(prefix) ? propertyName
								: prefix + "." + propertyName;
						if (hasText(propertyName)) {
							// If it is a map, the data will not be fetched
							// fix issue #91
							if (Collection.class.isAssignableFrom(field.getType())
									|| Map.class.isAssignableFrom(field.getType())) {
								bindContainer(prefix, propertyName, configProperties,
										propertyValues);
								return;
							}
							if (containsDescendantOf(configProperties.keySet(),
									propertyName) && !isUnbindableBean(field.getType())) {
								bindBean(propertyName, field.getType(), configProperties,
										propertyValues);
								return;
							}

							if (configProperties.containsKey(propertyName)) {
								String propertyValue = String
										.valueOf(configProperties.get(propertyName));
								propertyValues.add(field.getName(), propertyValue);
							}
						}
					}
				});
		return propertyValues;
	}

	public static Properties resolveProperties(NacosProperties nacosProperties,
			PropertyResolver propertyResolver) {
		return resolveProperties(nacosProperties, propertyResolver, null);
	}

	public static Properties resolveProperties(NacosProperties nacosProperties,
			PropertyResolver propertyResolver, Properties defaultProperties) {

		Map<String, Object> attributes = getAnnotationAttributes(nacosProperties);

		return resolveProperties(attributes, propertyResolver, defaultProperties);

	}

	/**
	 * {@link #resolveProperties(Map, PropertyResolver) Resolve} placeholders of
	 * {@link NacosProperties @NacosProperties}'s attributes via specified
	 * {@link PropertyResolver} if present, or try to
	 * {@link #merge(Properties, Properties) merge} from default properties
	 *
	 * @param attributes {@link NacosProperties @NacosProperties}'s attributes
	 * @param propertyResolver the resolver of properties' placeholder
	 * @param defaultProperties default properties
	 * @return a new resolved {@link Properties} properties
	 * @see #resolveProperties(Map, PropertyResolver)
	 */
	public static Properties resolveProperties(Map<String, Object> attributes,
			PropertyResolver propertyResolver, Properties defaultProperties) {

		if (CollectionUtils.isEmpty(attributes)) {
			return defaultProperties;
		}

		Properties resolveProperties = resolveProperties(attributes, propertyResolver);

		merge(resolveProperties, defaultProperties);

		return resolveProperties;
	}

	/**
	 * Resolve placeholders of properties via specified {@link PropertyResolver} if
	 * present
	 *
	 * @param properties The properties
	 * @param propertyResolver {@link PropertyResolver} instance, for instance,
	 *     {@link Environment}
	 * @return a new instance of {@link Properties} after resolving.
	 */
	public static Properties resolveProperties(Map<?, ?> properties,
			PropertyResolver propertyResolver) {
		PropertiesPlaceholderResolver propertiesPlaceholderResolver = new PropertiesPlaceholderResolver(
				propertyResolver);
		return propertiesPlaceholderResolver.resolve(properties);
	}

	/**
	 * Merge Nacos Properties If any property from target properties is absent
	 *
	 * @param targetProperties {@link Properties target Properties}
	 * @param sourceProperties {@link Properties source Properties}
	 */
	protected static void merge(Properties targetProperties,
			Properties sourceProperties) {

		if (CollectionUtils.isEmpty(sourceProperties)) {
			return;
		}

		for (Map.Entry entry : sourceProperties.entrySet()) {
			String propertyName = (String) entry.getKey();
			if (!targetProperties.containsKey(propertyName)) {
				String propertyValue = (String) entry.getValue();
				targetProperties.setProperty(propertyName, propertyValue);
			}
		}

	}

	/**
	 * Get content from {@link ConfigService} via dataId and groupId
	 *
	 * @param configService {@link ConfigService}
	 * @param dataId dataId
	 * @param groupId groupId
	 * @return If available , return content , or <code>null</code>
	 */
	public static String getContent(ConfigService configService, String dataId,
			String groupId) {
		String content = null;
		try {
			content = configService.getConfig(dataId, groupId, DEFAULT_TIMEOUT);
		}
		catch (NacosException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Can't get content from dataId : " + dataId + " , groupId : "
						+ groupId, e);
			}
		}
		return content;
	}

	/**
	 * bind properties to bean
	 *
	 * @param propertyName propertyName
	 * @param target bind target
	 * @param configProperties config context
	 * @param propertyValues {@link MutablePropertyValues}
	 */
	private static void bindBean(String propertyName, Class<?> target,
			Map<String, Object> configProperties, MutablePropertyValues propertyValues) {
		Object propertyValue = configProperties.get(propertyName);
		if (propertyValue != null) {
			propertyValues.add(propertyName, propertyValue);
		}
		if (isUnbindableBean(target)) {
			return;
		}

		Field[] fields = target.getDeclaredFields();
		for (Field field : fields) {
			String mergePropertyName = propertyName + "."
					+ NacosUtils.resolvePropertyName(field);
			bindBean(mergePropertyName, field.getType(), configProperties,
					propertyValues);
		}

	}

	private static boolean containsDescendantOf(Set<String> names, String propertyName) {
		for (String name : names) {
			if (name.startsWith(propertyName + ".")) {
				return true;
			}
		}
		return false;
	}

	private static boolean isUnbindableBean(Class<?> resolved) {
		if (resolved.isPrimitive() || NON_BEAN_CLASSES.contains(resolved)) {
			return true;
		}
		return resolved.getName().startsWith("java.");
	}

	/**
	 * Simple solutions to support {@link Map} or {@link Collection}
	 *
	 * @param fieldName property name
	 * @param configProperties config context
	 * @param propertyValues {@link MutablePropertyValues}
	 */
	private static void bindContainer(String prefix, String fieldName,
			Map<String, Object> configProperties, MutablePropertyValues propertyValues) {
		String regx1 = fieldName + "\\[(.*)\\]";
		String regx2 = fieldName + "\\..*";
		Pattern pattern1 = Pattern.compile(regx1);
		Pattern pattern2 = Pattern.compile(regx2);
		Set<String> enumeration = configProperties.keySet();
		for (Object item : enumeration) {
			final String s = String.valueOf(item);
			String name = StringUtils.isEmpty(prefix) ? s : s.replace(prefix + ".", "");
			Object value = configProperties.get(s);
			if (configProperties.containsKey(fieldName)) {
				// for example: list=1,2,3,4,5 will be into here
				bindContainer(prefix, fieldName,
						listToProperties(fieldName,
								String.valueOf(configProperties.get(fieldName))),
						propertyValues);
			}
			else if (pattern1.matcher(s).find()) {
				propertyValues.add(name, value);
			}
			else if (pattern2.matcher(s).find()) {
				int index = s.indexOf('.');
				if (index != -1) {
					String key = s.substring(index + 1);
					propertyValues.add(s.substring(0, index) + "[" + key + "]", value);
				}
			}
		}
	}

	/**
	 * convert list=1,2,3,4 to list[0]=1, list[1]=2, list[2]=3, list[3]=4
	 *
	 * @param fieldName fieldName
	 * @param content content
	 * @return {@link Properties}
	 */
	private static Map<String, Object> listToProperties(String fieldName,
			String content) {
		String[] splits = content.split(",");
		int index = 0;
		Map<String, Object> properties = new LinkedHashMap<String, Object>();
		for (String s : splits) {
			properties.put(fieldName + "[" + index + "]", s.trim());
			index++;
		}
		return properties;
	}

	private static String resolvePropertyName(Field field) {
		// Ignore property name if @NacosIgnore present
		if (getAnnotation(field, NacosIgnore.class) != null) {
			return null;
		}
		NacosProperty nacosProperty = getAnnotation(field, NacosProperty.class);
		// If @NacosProperty present ,return its value() , or field name
		return nacosProperty != null ? nacosProperty.value() : field.getName();
	}

	public static <T> Class<T> resolveGenericType(Class<?> declaredClass) {
		ParameterizedType parameterizedType = (ParameterizedType) declaredClass
				.getGenericSuperclass();
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		return (Class<T>) actualTypeArguments[0];
	}

	public static Map<String, Object> toProperties(String text) {
		return toProperties(text, "properties");
	}

	public static Map<String, Object> toProperties(String text, String type) {
		return toProperties("", "", text, type);
	}

	public static Map<String, Object> toProperties(String dataId, String group,
			String text) {
		return toProperties(dataId, group, text, "properties");
	}

	/**
	 * XML configuration parsing to support different schemas
	 *
	 * @param dataId config dataId
	 * @param group config group
	 * @param text config context
	 * @param type config type
	 */
	public static Map<String, Object> toProperties(String dataId, String group,
			String text, String type) {
		type = type.toLowerCase();
		if ("yml".equalsIgnoreCase(type)) {
			type = "yaml";
		}
		return ConfigParseUtils.toProperties(dataId, group, text, type);
	}

}
