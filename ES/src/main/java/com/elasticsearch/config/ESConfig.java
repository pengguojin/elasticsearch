package com.elasticsearch.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.6/index.html
 * 
 * @author jin
 *
 */
@Configuration
public class ESConfig {
	@Bean
	public TransportClient client() throws UnknownHostException {
		InetSocketTransportAddress node = new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300);
		Settings setting = Settings.builder().put("cluster.name", "jin").build();
		TransportClient client = new PreBuiltTransportClient(setting);
		client.addTransportAddress(node);
		return client;
	}
}
