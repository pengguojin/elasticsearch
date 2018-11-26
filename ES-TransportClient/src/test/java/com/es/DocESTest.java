package com.es;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.ReindexAction;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 基础API使用
 * 
 * @author jin
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DocESTest {
	@Autowired
	private TransportClient client;
	private static final String ES_Index = "user";
	private static final String ES_Index1 = "user1";
	private static final String ES_TYPE = "teacher";

	/**
	 * Index-API：通过json创建索引文档
	 */
	@Test
	public void Index() {
		// 指定ID插入
		String json = "{\"name\":\"张三\", \"age\": 12, \"sex\": \"男\"}";
		IndexResponse response = client.prepareIndex(ES_Index, ES_TYPE, "1").setSource(json, XContentType.JSON).get();
		System.out.println(response.getResult());

		// ES自定义ID插入
		String json1 = "{\"name\":\"李四\", \"age\": 13, \"sex\": \"男\"}";
		IndexResponse response1 = client.prepareIndex(ES_Index, ES_TYPE).setSource(json1, XContentType.JSON).get();
		System.out.println(response1.getResult());
	}

	/**
	 * Get-API：根据ID获取记录
	 */
	@Test
	public void Get() {
		GetResponse response = client.prepareGet(ES_Index, ES_TYPE, "1").get();
		System.out.println(response.getSource().get("name"));
	}

	/**
	 * Delete-API：根据ID删除
	 */
	@Test
	public void Delete() {
		DeleteResponse response = client.prepareDelete(ES_Index, ES_TYPE, "AWcRHEoL9BYzlc3kPo9a").get();
		System.out.println(response.getResult());
	}

	/**
	 * Delete By Query-API：根据条件删除
	 */
	@Test
	public void DeleteByQuery() {
		// 条件
		QueryBuilder query = QueryBuilders.matchQuery("name", "张三");

		// 同步删除
		BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client).filter(query)
				.source(ES_Index).get();
		System.out.println(response.getDeleted());

		// 异步删除
		DeleteByQueryAction.INSTANCE.newRequestBuilder(client).source(ES_Index).filter(query)
				.execute(new ActionListener<BulkByScrollResponse>() {
					@Override
					public void onResponse(BulkByScrollResponse response) {
						System.out.println(response.getDeleted());
					}

					@Override
					public void onFailure(Exception e) {
						System.out.println(e.getMessage());
					}
				});
	}

	/**
	 * Update-API：更新
	 */
	@Test
	public void Update() {
		// 1、使用UpdateRequest-非脚本方式更新
		try {
			UpdateRequest updateRequest = new UpdateRequest(ES_Index, ES_TYPE, "AWcRIZJC9BYzlc3kPo9c")
					.doc(XContentFactory.jsonBuilder().startObject().field("name", "小红").endObject());
			UpdateResponse response = client.update(updateRequest).get();
			System.out.println(response.getResult());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		// 2、使用UpdateRequest-脚本方式更新
		try {
			UpdateRequest updateRequest1 = new UpdateRequest(ES_Index, ES_TYPE, "AWcRIZJC9BYzlc3kPo9c")
					.script(new Script("ctx._source.name=\"小兰\""));
			UpdateResponse response2 = client.update(updateRequest1).get();
			System.out.println(response2.getResult());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		// 3、使用prepareUpdate-非脚本方式更新

		try {
			UpdateResponse response3 = client.prepareUpdate(ES_Index, ES_TYPE, "AWcRIZJC9BYzlc3kPo9c")
					.setDoc(XContentFactory.jsonBuilder().startObject().field("name", "小兰").endObject()).get();
			System.out.println(response3.getResult());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 4、使用prepareUpdate-脚本方式更新
		UpdateResponse response4 = client.prepareUpdate(ES_Index, ES_TYPE, "AWcRIZJC9BYzlc3kPo9c")
				.setScript(new Script("ctx._source.name=\"小兰\"")).get();
		System.out.println(response4.getResult());

		// 5、upsert：如果索引不存在，则创建索引
		try {
			IndexRequest indexRequest = new IndexRequest(ES_Index, ES_TYPE, "1")
					.source(XContentFactory.jsonBuilder().startObject().field("name", "小李").endObject());
			UpdateRequest updateRequest = new UpdateRequest(ES_Index, ES_TYPE, "1")
					.doc(XContentFactory.jsonBuilder().startObject().field("name", "小兰1").endObject())
					.upsert(indexRequest);
			UpdateResponse response = client.update(updateRequest).get();
			System.out.println(response.getResult());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Multi Get-API
	 */
	@Test
	public void MultiGet() {
		// 指定ID插入
		String json = "{\"name\":\"张三\", \"age\": 12, \"sex\": \"男\",\"test\":\"dasd\"}";
		IndexResponse res = client.prepareIndex(ES_Index1, ES_TYPE, "1").setSource(json, XContentType.JSON).get();
		System.out.println(res.getResult());

		// 获取一个索引的一个ID的数据
		MultiGetResponse multiRes = client.prepareMultiGet().add(ES_Index, ES_TYPE, "1").get();

		// 获取一个索引的多个ID的数据
		MultiGetResponse multiRes2 = client.prepareMultiGet().add(ES_Index, ES_TYPE, "1", "2", "3").get();

		// 获取多个索引的数据
		MultiGetResponse multiRes3 = client.prepareMultiGet().add(ES_Index, ES_TYPE, "1").add(ES_Index1, ES_TYPE, "1")
				.get();

		for (MultiGetItemResponse itemResponse : multiRes) {
			GetResponse response = itemResponse.getResponse();
			if (response.isExists()) {
				System.out.println(response.getSourceAsString());
			}
		}
	}

	/**
	 * Bulk-API：批量插入
	 */
	@Test
	public void Bulk() {
		String[] jsons = { "{\"name\":\"张三1\", \"age\": 31, \"sex\": \"男\",\"test\":\"aaa\"}",
				"{\"name\":\"张三1\", \"age\": 32, \"sex\": \"男\",\"test\":\"daasd\"}",
				"{\"name\":\"张三2\", \"age\": 33, \"sex\": \"男\",\"test\":\"daasd\"}",
				"{\"name\":\"张三3\", \"age\": 34, \"sex\": \"男\",\"test\":\"daasd\"}",
				"{\"name\":\"张三4\", \"age\": 35, \"sex\": \"男\",\"test\":\"daasd\"}",
				"{\"name\":\"张三5\", \"age\": 36, \"sex\": \"男\",\"test\":\"daasd\"}",
				"{\"name\":\"张三6\", \"age\": 37, \"sex\": \"男\",\"test\":\"daasd\"}",
				"{\"name\":\"张三7\", \"age\": 38, \"sex\": \"男\",\"test\":\"daasd\"}",
				"{\"name\":\"张三8\", \"age\": 39, \"sex\": \"男\",\"test\":\"daasd\"}" };
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (String json : jsons) {
			bulkRequest.add(client.prepareIndex(ES_Index, ES_TYPE).setSource(json, XContentType.JSON));
		}
		BulkResponse bulkResponse = bulkRequest.get();
		if (bulkResponse.hasFailures()) {
			System.out.println("进来了");
		}
	}

	/**
	 * Bulk Processor-API：一个批量操作的进程，提供异步监听方法
	 */
	public void BulkProcess() {
		BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
				// 方法被执行之前
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				// 方法被执行之后
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				// 执行出错的监听方法
			}
		}).setBulkActions(10000) // 每次执行多少次
				.setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB)) // 每5MB刷新一次
				.setFlushInterval(TimeValue.timeValueSeconds(5)) // 请求提交后，5秒后执行刷新
				.setConcurrentRequests(1) // 是否允许并发执行，0表示不允许，1表示允许
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)) // 设置100毫秒重试一次，和重试次数3
				.build();

		// 添加批量操作方法
		try {
			bulkProcessor.add(new IndexRequest(ES_Index, ES_TYPE, "1")
					.source(XContentFactory.jsonBuilder().startObject().field("name", "小李").endObject()));

			bulkProcessor.add(new DeleteRequest(ES_Index, ES_TYPE, "2"));
			bulkProcessor.flush();
			bulkProcessor.awaitClose(300, TimeUnit.MILLISECONDS);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	// https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-update-by-query.html

	/**
	 * Update By Query：条件更新
	 */
	@Test
	public void UpdateByQuery() {
		// 更新test等于daasd的，根据age排序的，前3条记录
		UpdateByQueryRequestBuilder updateByQuery = UpdateByQueryAction.INSTANCE.newRequestBuilder(client);
		updateByQuery.source(ES_Index) // 索引
				.filter(QueryBuilders.termQuery("test", "daasd")) // 条件
				.size(3) // 更新条数
				.script(new Script("ctx._source.name = '小兰'")) // 更新内容
				.source().addSort("age	", SortOrder.DESC); // 排序;
		BulkByScrollResponse response = updateByQuery.get();
		System.out.println(response.getUpdated());
	}

	/**
	 * Reindex-API：复制索引
	 */
	@Test
	public void Reindex() {
		BulkByScrollResponse response = ReindexAction.INSTANCE.newRequestBuilder(client) //
				.source(ES_Index) // 源索引
				.destination(ES_Index1) // 目的索引
				.filter(QueryBuilders.matchQuery("test", "daasd")).get(); // 条件
		System.out.println(response.getStatus());
	}
}
