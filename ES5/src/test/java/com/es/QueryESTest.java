package com.es;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * ES查询条件
 * 
 * @author jin
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class QueryESTest {
	@Autowired
	private TransportClient client;
	private static final String ES_Index = "user";
	private static final String ES_Index1 = "user1";
	private static final String ES_TYPE = "teacher";

	/**
	 * MatchQuery：用于执行全文查询的标准查询，包括模糊匹配和短语或邻近查询
	 * 
	 * multi_match query：匹配查询的多字段版本
	 * 
	 * common_terms query：一个更专业的查询，它会给不常见的词带来更多的偏好
	 * 
	 * query_string query：支持紧凑的Lucene查询字符串语法，允许在单个查询字符串中指定.|OR|NOT条件和多字段搜索
	 * 
	 * simple_query_string：一种更简单、更健壮的query_string语法
	 */
	@Test
	public void Query() {

	}

	/**
	 * 模糊查询
	 */
	@Test
	public void Wildcar() {
		QueryBuilder query = QueryBuilders.wildcardQuery("name", "*1*");
		SearchResponse response = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(query).get();

		for (SearchHit his : response.getHits().getHits()) {
			System.out.println(his.getSource());
		}
	}

	/**
	 * Range-范围查询
	 */
	@Test
	public void Range() {
		// gte, gt, lt, lte
		// 等于小于，小于，大于，等于大于
		// QueryBuilder query = QueryBuilders.rangeQuery("age").gte(32).lte(35);
		QueryBuilder query = QueryBuilders.rangeQuery("age").from(32).to(35).includeLower(true).includeUpper(false);
		SearchResponse response = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(query).get();

		for (SearchHit his : response.getHits().getHits()) {
			System.out.println(his.getSource());
		}
	}

	/**
	 * Exists-判断某个字段是否存在，相当于is_null
	 */
	@Test
	public void Exists() {
		QueryBuilder query = QueryBuilders.existsQuery("name");
		SearchResponse response = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(query).get();
		for (SearchHit searchHit : response.getHits()) {
			System.out.println(searchHit);
		}
	}

	/**
	 * 正在表达式查询
	 */
	@Test
	public void Regexp() {
		// 匹配含有数字的
		QueryBuilder query = QueryBuilders.regexpQuery("name", "[0-9]+");
		SearchResponse response = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(query).get();
		for (SearchHit his : response.getHits().getHits()) {
			System.out.println(his.getSource());
		}
	}

	/**
	 * Bool-合并查询条件
	 * 
	 * must：多个查询条件的完全匹配,相当于 and。
	 * 
	 * must_not：多个查询条件的相反匹配，相当于 not。
	 * 
	 * should：至少有一个查询条件匹配, 相当于 or。
	 */
	@Test
	public void Bool() {
		QueryBuilder query1 = QueryBuilders.wildcardQuery("name", "*1*");
		QueryBuilder query2 = QueryBuilders.termQuery("test", "daasd");

		QueryBuilder query = QueryBuilders.boolQuery().must(query1).must(query2);
		SearchResponse response = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(query).get();
		for (SearchHit his : response.getHits().getHits()) {
			System.out.println(his.getSource());
		}
	}
}
