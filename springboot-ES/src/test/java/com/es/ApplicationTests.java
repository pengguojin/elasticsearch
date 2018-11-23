package com.es;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.es.data.UserEntity;

import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootEsApplicationTests {

	@Autowired
	private JestClient jestClient;

	@Test
	public void testIndex() {
		UserEntity user = new UserEntity();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		user.setId(uuid);
		user.setAge(30);
		user.setName("彭国津");
		user.setSex("男");
		// 1、给索引保存文档
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		Index index = new Index.Builder(user).index(sf.format(cal.getTime())).type("venus").build();
		try {
			jestClient.execute(index);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 测试搜索
	@Test
	public void testSearch() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		// 查询表达式
		String sql = "{\"query\": {\"match\": {\"age\": 30}},\"size\": 2}";
		String groupBySql = "{\"size\": 0,\"aggs\": {\"popular_colors\": {\"terms\": {\"field\": \"age\"}}}}";
		// 构建搜索方法
		Search search = new Search.Builder(sql).addIndex(sf.format(cal.getTime())).addType("venus").build();

		Search groupBy = new Search.Builder(groupBySql).addIndex(sf.format(cal.getTime())).addType("venus").build();
		// 执行
		try {
			SearchResult result = jestClient.execute(search);
			SearchResult groupByresult = jestClient.execute(groupBy);
			System.out.println(result.getJsonString());
			System.out.println(groupByresult.getJsonString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDelete() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		Delete delete = new Delete.Builder("AWXmc5zrckXYARqwSHGy").index(sf.format(cal.getTime())).type("venus")
				.build();
		try {
			DocumentResult result = jestClient.execute(delete);
			System.out.println(result.getJsonString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
