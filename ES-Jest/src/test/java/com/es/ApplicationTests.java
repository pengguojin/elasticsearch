package com.es;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.es.data.AppTargetDataChildNewest;
import com.es.data.AppTargetDataParentNewest;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.DeleteIndex;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Autowired
	private JestClient jestClient;

	private static final String APP_TARGET_DATA_PARENT_NEWEST = "app_target_data_parent_newest";
	private static final String APP_TARGET_DATA_CHILD_NEWEST = "app_target_data_child_newest";
	private static final String TYPE = "venus";

	@Test
	public void testIndex() {

		String json = "{\"data_id\": \"5\", \"unit_code\": \"10001\", \"target_name\": \"ReceivingFlow\", \"target_value\": 24.6, \"create_time\": 1541987442000, \"data_type\": \"int\"}";

		JSONObject jsonObject = JSON.parseObject(json);
		// 1、给索引保存文档
		Index index = new Index.Builder(jsonObject).index(APP_TARGET_DATA_PARENT_NEWEST).type("venus").build();
		try {
			jestClient.execute(index);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIndexBulk() throws InterruptedException {
		DecimalFormat sdf = new DecimalFormat("0.00");

		DeleteIndex delete = new DeleteIndex.Builder(APP_TARGET_DATA_PARENT_NEWEST).type(TYPE).build();
		DeleteIndex deleteChild = new DeleteIndex.Builder(APP_TARGET_DATA_CHILD_NEWEST).type(TYPE).build();
		try {
			jestClient.execute(delete);
			jestClient.execute(deleteChild);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String[] unitCode = { "10001", "10002" };

		// 父指标
		Map<String, String> mapParent = new HashMap<String, String>();
		mapParent.put("ReceivingFlow", "netflow");
		mapParent.put("AbnormalFlow", "netflow");
		mapParent.put("InterceptDDosNum", "netflow");
		mapParent.put("ProtocolTraffic", "netflow");
		mapParent.put("AttackIPInfo", "netflow");
		mapParent.put("DesktopExAlarmInfo", "netflow");// 主机异常警报信息/异常流量报警类别统计
		mapParent.put("NetworkSecurityNum", "desktopDataSecret");
		mapParent.put("RegisterTerminalNum", "desktopDataSecret");
		mapParent.put("ViolationTerminalNum", "desktopDataSecret");
		mapParent.put("OnlineOfflineNum", "desktopDataSecret");
		mapParent.put("other1", "desktopDataSecret");

		// 子指标-父指标对应
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("FlowDhcp", "ProtocolTraffic");
		map.put("FlowPpp", "ProtocolTraffic");
		map.put("Flow802", "ProtocolTraffic");
		map.put("FlowPppoe", "ProtocolTraffic");
		map.put("winxp", "DesktopExAlarmInfo");
		map.put("win7", "DesktopExAlarmInfo");
		map.put("win8", "DesktopExAlarmInfo");
		map.put("win10", "DesktopExAlarmInfo");
		map.put("AttackIPInfo1", "AttackIPInfo");
		map.put("AttackIPInfo2", "AttackIPInfo");
		map.put("AttackIPInfo3", "AttackIPInfo");
		map.put("AttackIPInfo4", "AttackIPInfo");
		map.put("AttackIPInfo5", "AttackIPInfo");
		map.put("AttackIPInfo6", "AttackIPInfo");
		map.put("AttackIPInfo7", "AttackIPInfo");
		map.put("AttackIPInfo8", "AttackIPInfo");
		map.put("AttackIPInfo9", "AttackIPInfo");
		map.put("AttackIPInfo10", "AttackIPInfo");
		map.put("AttackIPInfo11", "AttackIPInfo");

		for (String unit : unitCode) {
			// 插入父表
			List<AppTargetDataParentNewest> list = new ArrayList<AppTargetDataParentNewest>();
			Builder builder = new Bulk.Builder().defaultIndex(APP_TARGET_DATA_PARENT_NEWEST).defaultType(TYPE);
			for (Map.Entry<String, String> parent : mapParent.entrySet()) {
				AppTargetDataParentNewest parentEntity = new AppTargetDataParentNewest();
				String uuid = UUID.randomUUID().toString().replace("-", "");
				parentEntity.setData_id(uuid);
				parentEntity.setUnit_code(unit);
				parentEntity.setApp_code(parent.getValue());
				parentEntity.setTarget_name(parent.getKey());
				parentEntity.setTarget_value(sdf.format(Math.random() * 100));
				parentEntity.setCreate_time(System.currentTimeMillis());
				parentEntity.setData_type("int");
				builder.addAction(new Index.Builder(parentEntity).build());
				list.add(parentEntity);

			}
			Bulk bulk = builder.build();

			// 插入子表
			Builder buildeC = new Bulk.Builder().defaultIndex(APP_TARGET_DATA_CHILD_NEWEST).defaultType(TYPE);
			for (Map.Entry<String, Object> m : map.entrySet()) {
				for (AppTargetDataParentNewest p : list) {
					if (p.getTarget_name().equals(m.getValue())) {
						String dataId = p.getData_id();
						AppTargetDataChildNewest child = new AppTargetDataChildNewest();
						String uuid = UUID.randomUUID().toString().replace("-", "");
						child.setRecord_id(uuid);
						child.setData_id(dataId);
						child.setChild_name(m.getKey());
						child.setCreate_time(System.currentTimeMillis());
						if (m.getValue().equals("AttackIPInfo")) {
							child.setTarget_value("{\"ip\":\"172.168.11.11\",\"department\":\"广东电网\",\"attacksNum\":"
									+ Math.round(Math.random() * 100) + "}");
						} else {
							child.setTarget_value(sdf.format(Math.random() * 100));
						}
						child.setOrderno(1);

						buildeC.addAction(new Index.Builder(child).build());

					}
				}
			}
			Bulk bulkC = buildeC.build();

			try {
				Thread.sleep(2000);
				jestClient.execute(bulk);
				jestClient.execute(bulkC);
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	@Test
	public void testJoin() {
	}
}
