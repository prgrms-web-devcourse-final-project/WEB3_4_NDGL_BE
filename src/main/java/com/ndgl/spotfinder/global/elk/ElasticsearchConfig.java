package com.ndgl.spotfinder.global.elk;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
@ConditionalOnProperty(
	name = {
		"spring.elasticsearch.uris",
		"spring.elasticsearch.username",
		"spring.elasticsearch.password"
	},
	matchIfMissing = false
)
public class ElasticsearchConfig {
	@Value("${spring.elasticsearch.uris}")
	private String elasticsearchUri;

	@Value("${spring.elasticsearch.username}")
	private String username;

	@Value("${spring.elasticsearch.password}")
	private String password;

	@Bean
	public RestClient restClient() {

		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
			new UsernamePasswordCredentials(username, password));

		return RestClient.builder(HttpHost.create(elasticsearchUri))
			.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
				.setDefaultCredentialsProvider(credentialsProvider))
			.build();
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
