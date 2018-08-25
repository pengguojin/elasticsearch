package com.elasticsearch.config;

import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.ElasticSearchDruidDataSourceFactory;

public class ESJdbcConfig {

	public void JdbcConfig() {
		Properties properties = new Properties();
		ESConfig esConfig = new ESConfig();
		System.out.println("===================getEsHost=" + esConfig.getEsHost());
		// jdbc:elasticsearch://localhost:9300/user
		properties.put(PROP_URL, "jdbc:elasticsearch://localhost:9300/user");
		// properties.put("PROP_VALIDATIONQUERY", "select 1 ");
		try {
			DruidDataSource dds = (DruidDataSource) ElasticSearchDruidDataSourceFactory.createDataSource(properties);
			Connection connection = dds.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT age from user ");
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				System.out.println("==============name============" + resultSet.getString("name"));
			}
			ps.close();
			connection.close();
			dds.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
