package com.restclient;

import java.util.Arrays;
import java.util.Objects;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ElasticsearchRestClient {
	/**
	 * 使用冒号隔开ip和端口
	 */
	@Value("${elasticsearch.ip}")
	String[] ipAddress;

	private static final int ADDRESS_LENGTH = 2;

	private static final String HTTP_SCHEME = "http";

	@Bean
	public RestClient restClient() {
		HttpHost[] hosts = Arrays.stream(ipAddress).map(this::makeHttpHost).filter(Objects::nonNull)
				.toArray(HttpHost[]::new);
		log.debug("hosts:{}", Arrays.toString(hosts));
		return RestClient.builder(hosts).build();
	}

	@Bean(name = "highLevelClient")
	public RestHighLevelClient highLevelClient(@Autowired RestClient restClient) {
		return new RestHighLevelClient(restClient);
	}

	private HttpHost makeHttpHost(String s) {
		String[] address = s.split(":");
		if (address.length == ADDRESS_LENGTH) {
			String ip = address[0];
			int port = Integer.parseInt(address[1]);
			return new HttpHost(ip, port, HTTP_SCHEME);
		} else {
			return null;
		}
	}

}
