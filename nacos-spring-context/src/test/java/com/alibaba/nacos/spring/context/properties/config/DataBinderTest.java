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

package com.alibaba.nacos.spring.context.properties.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;

/**
 * {@link DataBinder} Test
 * @author SuperZ1999
 * @date 2023/9/28
 */
public class DataBinderTest {
    @Test
    public void test() throws BindException {
        People people = new People();
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.add("name", "SuperZ1999");
        propertyValues.add("age", 24);
        DataBinder dataBinder = new DataBinder(people);
        dataBinder.setAutoGrowNestedPaths(false);
        dataBinder.setIgnoreInvalidFields(false);
        dataBinder.setIgnoreUnknownFields(true);
        dataBinder.bind(propertyValues);
        dataBinder.close();

        Assert.assertEquals("SuperZ1999", people.getName());
        Assert.assertEquals(24, people.getAge());
    }

    static class People {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "People{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}