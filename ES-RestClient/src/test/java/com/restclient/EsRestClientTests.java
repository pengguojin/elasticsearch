package com.restclient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EsRestClientTests {
	@Autowired
	private RestHighLevelClient restClient;

	/**
	 * Index-API：创建索引
	 */
	@Test
	public void index() {
		// json方式
		IndexRequest request = new IndexRequest("user3", "teacher");
		String jsonString = "{" + "\"user\":\"测试\"," + "\"postDate\":\"2013-01-30\","
				+ "\"message\":\"trying out Elasticsearch\"" + "}";
		request.source(jsonString, XContentType.JSON);

		// Map方式
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("user", "kimchy");
		jsonMap.put("postDate", new Date());
		jsonMap.put("message", "trying out Elasticsearch");
		IndexRequest request2 = new IndexRequest("user4", "teacher").source(jsonMap);

		try {
			IndexResponse index = restClient.index(request);
			log.info("index is {}", index.getId());

			IndexResponse index2 = restClient.index(request2);
			log.info("index is {}", index2.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search-API：查询
	 */
	@Test
	public void Search() {
		SearchRequest searchRequest = new SearchRequest("user").types("teacher");
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// 中文分词，需要把中文分隔成单个字符查询
		String name = "张三1";
		String[] names = name.split("");
		Arrays.asList(names).forEach(n -> {
			sourceBuilder.query(QueryBuilders.termQuery("name", n));
		});
		// 设置超时
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(sourceBuilder);
		try {
			SearchResponse response = restClient.search(searchRequest);
			Arrays.stream(response.getHits().getHits()).forEach(i -> {
				log.info("ID is {}", i.getId());
				log.info("result is {}", i.getSourceAsString());
			});
			log.info("result total {}", response.getHits().totalHits);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
