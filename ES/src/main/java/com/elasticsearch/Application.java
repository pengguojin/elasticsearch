package com.elasticsearch;

import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication

@RestController
public class Application {
	@Autowired
	private TransportClient client;

	@GetMapping(value = "/get/user/detail")
	@ResponseBody
	public Map<String, Object> get(@RequestParam(value = "id", defaultValue = "") String id) {
		GetResponse response = client.prepareGet("user", "teacher", id).get();
		Map<String, Object> map = response.getSource();
		if (!response.isExists()) {
			return null;
		} else {
			return map;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
