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
		IndexRequest request = new IndexRequest("user3", "teacher");
		String jsonString = "{" + "\"user\":\"测试\"," + "\"postDate\":\"2013-01-30\","
				+ "\"message\":\"trying out Elasticsearch\"" + "}";
		request.source(jsonString, XContentType.JSON);

		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("user", "kimchy");
		jsonMap.put("postDate", new Date());
		jsonMap.put("message", "trying out Elasticsearch");
		IndexRequest request2 = new IndexRequest("user4", "teacher").source(jsonMap);

		try {
			IndexResponse index = restClient.index(request);
			System.out.println(index.getResult());

			IndexResponse index2 = restClient.index(request2);
			System.out.println(index2.getResult());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 测试是否连接上ES
	 */
	@Test
	public void contextLoads() {
		SearchRequest searchRequest = new SearchRequest("user");
		searchRequest.types("teacher");
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.termQuery("age", "31"));
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(sourceBuilder);
		try {
			SearchResponse response = restClient.search(searchRequest);
			Arrays.stream(response.getHits().getHits()).forEach(i -> {
				System.out.println(i.getIndex());
				System.out.println(i.getSourceAsString());
				System.out.println(i.getType());
			});
			System.out.println(response.getHits().totalHits);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
