package com.elasticsearch.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.6/index.html
 *
 * @author jin
 */
@Configuration
@PropertySource(value = { "classpath:application.properties" }, encoding = "utf-8")
public class ESConfig {
	@Autowired
	Environment env;

	private String esHost;

	private Integer esPort;

	private String esClusterName;

	public String getEsHost() {
		return esHost;
	}

	public Integer getEsPort() {
		return esPort;
	}

	public String getEsClusterName() {
		return esClusterName;
	}

	@Value("${ES.host}")
	public void setEsHost(String esHost) {
		this.esHost = esHost;
	}

	@Value("${ES.port}")
	public void setEsPort(Integer esPort) {
		this.esPort = esPort;
	}

	@Value("${ES.cluster.name}")
	public void setEsClusterName(String esClusterName) {
		this.esClusterName = esClusterName;
	}

	@Bean
	public TransportClient client() throws UnknownHostException {
		System.out.println("==============================cluster.name" + env.getProperty("ES.cluster.name"));
		TransportClient client = new PreBuiltTransportClient(
				Settings.builder().put("cluster.name", esClusterName).build());
		client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
		return client;
	}

}
