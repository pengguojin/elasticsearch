package com.es;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
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
	private SearchDao searchDao;

	@Test
	public void testEs() throws SQLFeatureNotSupportedException, SqlParseException {
		String sql = "select * from user";
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		SearchResponse response = (SearchResponse) searchDao.explain(sql).explain().getBuilder().get();
		for (SearchHit hit : response.getHits().getHits()) {
			Map<String, Object> map = hit.getSource();
			map.put("id", hit.getId());
			list.add(map);
		}
		System.out.println(list);
	}

}
