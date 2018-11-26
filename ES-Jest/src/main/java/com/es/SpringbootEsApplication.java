package com.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * springboot默认支持两种技术和ES交互；
 * 
 * 1、jest（默认不生效）：需要导入jest的工具包（io.searchbox.client.JestClient）
 * 
 * 2、springdata elasticsearch
 * 
 * 1)、client节点信息clusterNode；clusterName；
 * 
 * 2)、ElasticsearchTemplate操作ES
 * 
 * 3)、编写一个ElasticSearchRepository的子接口来操作ES
 *
 */
@SpringBootApplication
public class SpringbootEsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootEsApplication.class, args);
	}
}
