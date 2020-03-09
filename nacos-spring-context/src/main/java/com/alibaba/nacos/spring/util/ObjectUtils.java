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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import com.alibaba.nacos.api.config.annotation.NacosIgnore;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.ReflectionUtils;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.0
 */
public final class ObjectUtils {

	private ObjectUtils() {
	}

	public static void cleanMapOrCollectionField(final Object bean) {
		ReflectionUtils.doWithFields(bean.getClass(),
				new ReflectionUtils.FieldCallback() {

					@Override
					public void doWith(Field field)
							throws IllegalArgumentException, IllegalAccessException {
						field.setAccessible(true);
						if (field.isAnnotationPresent(NacosIgnore.class)) {
							return;
						}
						Class<?> type = field.getType();

						if (Map.class.isAssignableFrom(type)
								|| Collection.class.isAssignableFrom(type)) {
							field.set(bean, null);
						}
					}
				});
	}

	public static Object convertIfNecessary(ConfigurableListableBeanFactory beanFactory,
			Field field, Object value) {
		TypeConverter converter = beanFactory.getTypeConverter();
		return converter.convertIfNecessary(value, field.getType(), field);
	}

	public static Object convertIfNecessary(ConfigurableListableBeanFactory beanFactory,
			Method method, Object value) {
		Class<?>[] paramTypes = method.getParameterTypes();
		Object[] arguments = new Object[paramTypes.length];

		TypeConverter converter = beanFactory.getTypeConverter();

		if (arguments.length == 1) {
			return converter.convertIfNecessary(value, paramTypes[0],
					new MethodParameter(method, 0));
		}

		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = converter.convertIfNecessary(value, paramTypes[i],
					new MethodParameter(method, i));
		}

		return arguments;
	}

}
