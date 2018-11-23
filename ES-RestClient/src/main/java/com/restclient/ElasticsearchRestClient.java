package com.restclient;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springboot2.1后基于配置文件配置
 * 
 * @author jin
 *
 */
@Configuration
public class ElasticsearchRestClient {

	@Bean(name = "highLevelClient")
	public RestHighLevelClient highLevelClient(@Autowired RestClient restClient) {
		return new RestHighLevelClient(restClient);
	}

}
