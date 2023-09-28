package com.alibaba.nacos.spring.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.nacos.api.config.annotation.NacosIgnore;
import org.junit.Assert;
import org.junit.Test;

public class ObjectUtilsTest {
	@Test
	public void test_map_clean() {
		TestObj obj = new TestObj();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", "value");
		obj.setMap(map);
		List<String> list = new ArrayList<>();
		list.add("element");
		obj.setList(list);

		Assert.assertNotNull(map.get("key"));
		Assert.assertNotNull(obj.map);
		Assert.assertNotNull(obj.list);
		ObjectUtils.cleanMapOrCollectionField(obj);
		map = obj.map;
		Assert.assertNull(map);

		Assert.assertNull(obj.map);
		Assert.assertNotNull(obj.list);
	}

	private static class TestObj {

		private Map<String, Object> map;

		@NacosIgnore
		private List<String> list;

		public List<String> getList() {
			return list;
		}

		public void setList(List<String> list) {
			this.list = list;
		}

		public Map<String, Object> getMap() {
			return map;
		}
		public void setMap(Map<String, Object> map) {
			this.map = map;
		}
	}
}
