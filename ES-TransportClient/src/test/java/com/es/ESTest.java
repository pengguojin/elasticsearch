package com.es;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试ESQL
 * 
 * @author jin
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ESTest {
	@Autowired
	private TransportClient client;
	private static final String APP_TARGET_DATA_PARENT_NEWEST = "app_target_data_parent_newest";
	private static final String APP_TARGET_DATA_CHILD_NEWEST = "app_target_data_child_newest";
	private static final String TYPE = "venus";

	@Test
	public void insertEs() {

	}

	@Test
	public void testEs() throws SQLFeatureNotSupportedException, SqlParseException {
		SearchDao dao = new SearchDao(client);
		String sql = "select * from user";
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		SearchResponse response = (SearchResponse) dao.explain(sql).explain().getBuilder().get();
		for (SearchHit hit : response.getHits().getHits()) {
			Map<String, Object> map = hit.getSource();
			map.put("id", hit.getId());
			list.add(map);
		}
		System.out.println(list);
	}

	@Test
	public void testJoin() {
		TermQueryBuilder term = QueryBuilders.termQuery("data_id", "aaf5981e3b8046ea9cb2ab4a97e9ca9d");
		QueryBuilder query = JoinQueryBuilders.hasParentQuery(APP_TARGET_DATA_CHILD_NEWEST, term, false);

		SearchResponse get = client.prepareSearch(APP_TARGET_DATA_PARENT_NEWEST).setTypes(TYPE).setQuery(term).get();
		for (SearchHit hit : get.getHits().getHits()) {
			Map<String, Object> map = hit.getSource();
			System.out.println(map);
		}
	}
}
