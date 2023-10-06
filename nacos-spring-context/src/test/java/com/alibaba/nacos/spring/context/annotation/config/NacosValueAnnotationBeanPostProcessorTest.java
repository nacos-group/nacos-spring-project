/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.alibaba.nacos.spring.context.annotation.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Method;

/**
 * {@link NacosValueAnnotationBeanPostProcessor} Test
 * @author SuperZ1999
 * @date 2023/9/28
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NacosValueAnnotationBeanPostProcessorTest.class)
public class NacosValueAnnotationBeanPostProcessorTest {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Bean
    public NacosValueAnnotationBeanPostProcessor nacosValueAnnotationBeanPostProcessor() {
        return new NacosValueAnnotationBeanPostProcessor();
    }

    @Test
    public void testConvertIfNecessary() throws NoSuchMethodException {
        TypeConverter converter = beanFactory.getTypeConverter();
        Method method = NacosValueAnnotationBeanPostProcessorTest.class.getMethod("testMethodParameter", Integer.class);
        Integer integer = converter.convertIfNecessary("12", Integer.class, new MethodParameter(method, 0));
        System.out.println(integer);
        Assert.assertEquals(integer, Integer.valueOf(12));
    }

    public void testMethodParameter(Integer i) {
        System.out.println(i);
    }
}