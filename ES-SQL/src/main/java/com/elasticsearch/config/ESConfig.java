package com.elasticsearch.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.nlpcn.es4sql.SearchDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 
 * @author jin
 *
 */
@Configuration
public class ESConfig {
	@Autowired
	Environment env;

	@Bean
	public TransportClient client() throws UnknownHostException {
		String esClusterName = env.getProperty("ES.cluster.name");
		String host = env.getProperty("ES.host");
		int port = Integer.valueOf(env.getProperty("ES.port"));

		InetSocketTransportAddress node = new InetSocketTransportAddress(InetAddress.getByName(host), port);
		Settings setting = Settings.builder().put("cluster.name", esClusterName).build();
		TransportClient client = new PreBuiltTransportClient(setting);
		client.addTransportAddress(node);
		return client;
	}

	@Bean
	public SearchDao searchDao(@Autowired TransportClient client) throws UnknownHostException {
		return new SearchDao(client);
	}
}
