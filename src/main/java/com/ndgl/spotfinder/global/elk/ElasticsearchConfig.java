package com.ndgl.spotfinder.global.elk;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "spring.elasticsearch.uris", matchIfMissing = false)
public class ElasticsearchConfig {
	@Value("${spring.elasticsearch.uris}")
	private String elasticsearchUri;

	@Bean
	public RestClient restClient() {
		return RestClient.builder(HttpHost.create(elasticsearchUri)).build();
	}

	@Bean
	public ElasticsearchClient elasticsearchClient(RestClient restClient) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		ElasticsearchTransport transport = new RestClientTransport(
			restClient,
			new JacksonJsonpMapper(objectMapper)
		);

		return new ElasticsearchClient(transport);
	}
}
