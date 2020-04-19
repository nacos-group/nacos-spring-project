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

/**
 * @author mai.jh
 */
@NacosConfigurationProperties(dataId = "yaml_bean.yml", autoRefreshed = true, ignoreNestedProperties = true, type = ConfigType.YAML)
public class YamlBean {

	public static final String DATA_ID_YAML = "yaml_bean";

	private Student student;

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	@Override
	public String toString() {
		return "YamlBean{" + "student=" + student + '}';
	}

	public static class Student {

		private String name;
		private String num;

		private TestApp testApp;

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

		public TestApp getTestApp() {
			return testApp;
		}

		public void setTestApp(TestApp testApp) {
			this.testApp = testApp;
		}

		@Override
		public String toString() {
			return "Student{" + "name='" + name + '\'' + ", num='" + num + '\'' + ", testApp=" + testApp + '}';
		}
	}

	public static class TestApp {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "TestApp{" + "name='" + name + '\'' + '}';
		}
	}

}
