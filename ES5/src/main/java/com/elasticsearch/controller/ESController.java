package com.elasticsearch.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ESController {
	public final static String index = "user";
	public final static String type = "teacher";

	@Autowired
	private TransportClient client;

	/**
	 * 根据ID获取数据：http://localhost:8080/get/user/detail?id=
	 *
	 * @param id：主键值
	 * @return
	 */
	@GetMapping(value = "/get/user/detail")
	@ResponseBody
	public Map<String, Object> get(@RequestParam(value = "id") String id) {
		GetResponse response = client.prepareGet(index, type, id).get();
		Map<String, Object> map = response.getSource();
		if (!response.isExists()) {
			return null;
		} else {
			return map;
		}
	}

	/**
	 * 根据ID-删除数据： http://localhost:8080/delete_id/user
	 *
	 * @return
	 */
	@DeleteMapping(value = "/delete_id/user")
	@ResponseBody
	public ResponseEntity deleteById(@RequestParam(value = "id") String id) {
		DeleteResponse response = this.client.prepareDelete(index, type, id).get();
		return new ResponseEntity(response.getResult(), HttpStatus.OK);
	}

	/**
	 * 根据条件-删除数据： http://localhost:8080/delete_field/user
	 *
	 * @return
	 */
	@DeleteMapping(value = "/delete_field/user")
	@ResponseBody
	public ResponseEntity delete(@RequestParam(value = "field") String[] field,
			@RequestParam(value = "value") String[] value) {
		DeleteByQueryRequestBuilder delete = DeleteByQueryAction.INSTANCE.newRequestBuilder(client);
		for (int i = 0; i < field.length; i++) {
			delete.filter((QueryBuilders.matchQuery(field[i], value[i])));
		}
		BulkByScrollResponse response = delete.source(index).get();
		return new ResponseEntity(response.getDeleted(), HttpStatus.OK);
	}

	/**
	 * 根据多个ID查询：http://localhost:8080/multi/user
	 *
	 * @return
	 */
	@GetMapping(value = "/multi/user")
	@ResponseBody
	public List<Map<String, Object>> MultiGet() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
				.add(index, type, "AWSmDfbRmVyu9ehoO05w", "AWSmF5IJHH-PYuQ8Nvee").get();
		for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
			GetResponse response = itemResponse.getResponse();
			if (response.isExists()) {
				list.add(response.getSource());
			}
		}
		return list;
	}

	/**
	 * 分页查询：http://localhost:8080/search/user
	 *
	 * @return
	 */
	@GetMapping(value = "/search/user")
	@ResponseBody
	public List<Map<String, Object>> Search(@RequestParam(value = "pageIndex") int pageIndex,
			@RequestParam(value = "pageSize") int pageSize) {
		// new ESJdbcConfig().JdbcConfig();
		// System.out.println("进来了");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		SearchResponse response = client.prepareSearch(index).setTypes(type)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom((pageIndex - 1) * pageSize).setSize(pageSize)
				.setExplain(true).get();
		for (SearchHit hit : response.getHits().getHits()) {
			Map<String, Object> map = hit.getSource();
			map.put("id", hit.getId());
			list.add(map);
		}
		return list;
	}

	/**
	 * 插入数据：http://localhost:8080/insert/user
	 */
	@PostMapping(value = "/insert/user")
	@ResponseBody
	public ResponseEntity insert(@RequestParam(value = "name") String name, @RequestParam(value = "age") int age,
			@RequestParam(value = "course") int course) {
		try {
			XContentBuilder content = XContentFactory.jsonBuilder().startObject().field("name", name).field("age", age)
					.field("course", course).field("create_time", new Date().getTime()).endObject();
			IndexResponse response = this.client.prepareIndex(index, type).setSource(content).get();
			return new ResponseEntity(response.getResult(), HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 批量插入操作：http://localhost:8080/bulk_delete/user
	 *
	 * @return
	 */
	@PutMapping(value = "/bulk_insert/user")
	@ResponseBody
	public ResponseEntity BulkInsert() {
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		try {
			// 批量插入
			bulkRequest.add(client.prepareIndex(index, type)
					.setSource(XContentFactory.jsonBuilder().startObject().field("name", "张三")
							.field("create_time", new Date().getTime()).field("age", 33).field("course", 86)
							.endObject()));
			BulkResponse bulkResponse = bulkRequest.get();
			if (bulkResponse.hasFailures()) {
				return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity(HttpStatus.OK);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 更新数据：http://localhost:8080/update/user?id=AWSmDfbRmVyu9ehoO05w * @param
	 *
	 * @param id
	 *            ：主键值
	 * @return 更新成功个数
	 */
	@PutMapping(value = "/update/user")
	@ResponseBody
	public ResponseEntity update(@RequestParam(value = "id") String id) {
		try {
			UpdateRequest update = new UpdateRequest(index, type, id)
					.doc(XContentFactory.jsonBuilder().startObject().field("name", "张三").endObject());
			UpdateResponse response = client.update(update).get();
			return new ResponseEntity(HttpStatus.OK);
		} catch (IOException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 批量更新操作：http://localhost:8080/bulk_insert/user
	 *
	 * @return
	 */
	@PutMapping(value = "/bulk_update/user")
	@ResponseBody
	public ResponseEntity BulkUpdate() {
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		try {
			// 批量更新
			bulkRequest.add(client.prepareIndex(index, type, "AWSx1fFP6RedS6ykFbWd")
					.setSource(XContentFactory.jsonBuilder().startObject().field("name", "李四")
							.field("create_time", new Date().getTime()).field("age", 35).field("course", 88)
							.endObject()));
			BulkResponse bulkResponse = bulkRequest.get();
			if (bulkResponse.hasFailures()) {
				return new ResponseEntity(HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity(HttpStatus.OK);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
	}
}
