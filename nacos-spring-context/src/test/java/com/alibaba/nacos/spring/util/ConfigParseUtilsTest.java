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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.nacos.spring.util.parse.DefaultPropertiesConfigParse;
import com.alibaba.nacos.spring.util.parse.DefaultYamlConfigParse;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author liaochuntao
 */
public class ConfigParseUtilsTest {

	private static String dataId = "data-id-1";
	private static String group = "group";
	private static AtomicInteger atomicInteger = new AtomicInteger(0);

	@Test
	public void testXmlConfigParse() {
		String xmlConfig = "<xmlSign>\n" + "    <Students>\n" + "        <Student>\n"
				+ "            <Name>lct-1</Name>\n"
				+ "            <Num>1006010022</Num>\n"
				+ "            <Classes>major-1</Classes>\n"
				+ "            <Address>hangzhou</Address>\n"
				+ "            <Tel>123456</Tel>\n" + "        </Student>\n"
				+ "        <Student>\n" + "            <Name>lct-2</Name>\n"
				+ "            <Num>1006010033</Num>\n"
				+ "            <Classes>major-2</Classes>\n"
				+ "            <Address>shengzheng</Address>\n"
				+ "            <Tel>234567</Tel>\n" + "        </Student>\n"
				+ "        <Student>\n" + "            <Name>lct-3</Name>\n"
				+ "            <Num>1006010044</Num>\n"
				+ "            <Classes>major-3</Classes>\n"
				+ "            <Address>wenzhou</Address>\n"
				+ "            <Tel>345678</Tel>\n" + "        </Student>\n"
				+ "        <Student>\n" + "            <Name>lct-4</Name>\n"
				+ "            <Num>1006010055</Num>\n"
				+ "            <Classes>major-3</Classes>\n"
				+ "            <Address>wuhan</Address>\n"
				+ "            <Tel>456789</Tel>\n" + "        </Student>\n"
				+ "    </Students>\n" + "</xmlSign>";
		Assert.assertEquals(0,
				ConfigParseUtils.toProperties(dataId, group, xmlConfig, "xml").size());
		Assert.assertEquals(1, atomicInteger.get());
		Map<String, Object> properties = ConfigParseUtils.toProperties(xmlConfig, "XML");
		Assert.assertTrue(0 != properties.size());
		System.out.println(ConfigParse.class.isAssignableFrom(CustomerParse.class));
		System.out.println(properties);
	}

	@Test
	public void testPropertiesParser() {
		final String properties = "name=yihaomen-aaa\n" + "address=wuhan\n" + "\n" + "#App\n"
				+ "app.menus[0].title=Home\n" + "app.menus[0].name=Home\n"
				+ "app.menus[0].path=/\n" + "app.menus[1].title=Login\n"
				+ "app.menus[1].name=Login\n" + "app.menus[1].path=/login\n" + "\n"
				+ "app.compiler.timeout=5\n" + "app.compiler.output-folder=/temp/\n" + "\n"
				+ "app.error=/error/\n" + "\n" + "school=hangzhoudianzi \\\n" + "university";
		DefaultPropertiesConfigParse parse = new DefaultPropertiesConfigParse();
		Map<String, Object> p = parse.parse(properties);
		System.out.println(p);
	}

	@Test
	public void testYamlParser() {
		final String yaml = "students:\n" + "    - {name: lct-1,num: 12}\n"
				+ "    - {name: lct-2,num: 13}\n" + "    - {name: lct-3,num: 14}";

		DefaultYamlConfigParse parse = new DefaultYamlConfigParse();
		Map<String, Object> p = parse.parse(yaml);
		System.out.println(p);

		System.out.println(parse.parse("people:\n" + "  a: 1\n" + "  b: 1"));
	}

	public static class CustomerParse extends AbstractConfigParse {

		@Override
		public Map<String, Object> parse(String configText) {
			atomicInteger.incrementAndGet();
			return new LinkedHashMap<String, Object>();
		}

		@Override
		public String processType() {
			return "xml";
		}

		@Override
		public String dataId() {
			return dataId;
		}

		@Override
		public String group() {
			return group;
		}
	}

}
