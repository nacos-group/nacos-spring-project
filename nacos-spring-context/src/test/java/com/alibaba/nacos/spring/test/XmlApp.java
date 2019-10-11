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

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;

import java.util.List;

import static com.alibaba.nacos.spring.test.XmlApp.DATA_ID_XML;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@NacosConfigurationProperties(dataId = DATA_ID_XML, autoRefreshed = true, ignoreNestedProperties = true, type = ConfigType.XML)
public class XmlApp {

    public static final String DATA_ID_XML = "xml_app";

    private List<Student> students;

    public List<XmlApp.Student> getStudents() {
        return students;
    }

    public void setStudents(List<XmlApp.Student> students) {
        this.students = students;
    }

    @Override
    public String toString() {
        return "XmlApp{" +
                "students=" + students +
                '}';
    }

    public static class Student {

        private String name;
        private String num;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        @Override
        public String toString() {
            return "Student{" +
                    "name='" + name + '\'' +
                    ", num='" + num + '\'' +
                    '}';
        }
    }

}

