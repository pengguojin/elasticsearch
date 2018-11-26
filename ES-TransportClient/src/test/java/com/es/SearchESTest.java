package com.es;

import java.util.List;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentile;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.stats.StatsAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 查询API的使用
 * 
 * @author jin
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchESTest {
	@Autowired
	private TransportClient client;
	private static final String ES_Index = "user";
	private static final String ES_Index1 = "user1";
	private static final String ES_TYPE = "teacher";

	/**
	 * Scrolls-API：游标-分页查询
	 */
	@Test
	public void Scrolls() {
		QueryBuilder query = QueryBuilders.termQuery("test", "daasd");

		SearchResponse scroll = client.prepareSearch(ES_Index) // 设置索引
				.setQuery(query) // 设置查询条件
				.addSort("age", SortOrder.DESC) // 条件排序
				.setScroll(new TimeValue(60000))// 添加游标
				.setSize(5) // 设置每次循环数量大小
				.get();
		// 循环游标
		int i = 1;
		do {
			System.out.println("第" + i + "页开始");
			for (SearchHit hit : scroll.getHits().getHits()) {
				System.out.println(hit.getSource());
			}

			// 跳到下一个游标
			scroll = client.prepareSearchScroll(scroll.getScrollId()).setScroll(new TimeValue(60000)).execute()
					.actionGet();
			System.out.println("第" + i + "页结束");
			i++;
		} while (scroll.getHits().getHits().length != 0);
	}

	/**
	 * MultiSearch-API：混合查询
	 */
	@Test
	public void MultiSearch() {
		SearchRequestBuilder srb1 = client.prepareSearch().setQuery(QueryBuilders.termQuery("test", "daasd"));
		SearchRequestBuilder srb2 = client.prepareSearch().setQuery(QueryBuilders.matchQuery("test", "aaa"));

		MultiSearchResponse sr = client.prepareMultiSearch().add(srb1).add(srb2).get();

		for (MultiSearchResponse.Item item : sr.getResponses()) {
			SearchResponse response = item.getResponse();
			// 总数是某一个条件的总数
			System.out.println("总数=" + response.getHits().getTotalHits());
			// 查询出来的结果是条件筛选后的结果
			for (SearchHit hit : response.getHits().getHits()) {
				System.out.println(hit.getSource());
			}
		}
	}

	/**
	 * AggRegations-API：多个聚合查询
	 */
	@Test
	public void AggRegations() {
		// 计算平均值
		AggregationBuilder agg = AggregationBuilders.avg("avgs").field("age");
		SearchResponse sr = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(agg).get();
		Avg avg = sr.getAggregations().get("avgs");
		System.out.println("平均年龄：" + avg.getValue());

		// 最大最小汇总等类似

		// 统计
		StatsAggregationBuilder agg1 = AggregationBuilders.stats("agg").field("age");
		SearchResponse sr1 = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(agg1).get();
		Stats stats = sr1.getAggregations().get("agg");
		System.out.println("平均值：" + stats.getAvg() + ",汇总值：" + stats.getCount() + ",最大值：" + stats.getMax() + ",最小值"
				+ stats.getMin());

		// 百分比
		PercentilesAggregationBuilder agg2 = AggregationBuilders.percentiles("agg").field("age");
		SearchResponse sr2 = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(agg2).get();
		Percentiles per = sr2.getAggregations().get("agg");
		for (Percentile p : per) {
			System.out.println("百分比：" + p.getPercent() + ",值：" + p.getValue());
		}

	}

	/**
	 * 分组查询
	 */
	@Test
	public void GroupBy() {
		// 分组查询
		TermsAggregationBuilder agg3 = AggregationBuilders.terms("ages").field("name")
				.subAggregation(AggregationBuilders.sum("count_age").field("age"));
		SearchResponse sr3 = client.prepareSearch(ES_Index).setTypes(ES_TYPE).setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(agg3).execute().actionGet();
		Terms term = sr3.getAggregations().get("ages");
		List<? extends Bucket> list = term.getBuckets();
		for (int i = 0; i < list.size(); i++) {
			Bucket b = list.get(i);
			System.out.println("key:" + b.getKey() + ",value:" + b.getKeyAsNumber());
		}
	}
}
