package com.ndgl.spotfinder.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestClientConfig {

	@Value("${spring.security.oauth2.client.provider.google.token-uri}")
	private String tokenUri;

	@Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
	private String userInfoUri;

	@Bean
	public RestClient googleRestClient() {
		log.info("tokenUri: {}", tokenUri);
		return RestClient.builder()
			.baseUrl(tokenUri)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.build();
	}

	@Bean
	public RestClient googleUserInfoRestClient() {
		log.info("userInfoUri: {}", userInfoUri);
		return RestClient.builder()
			.baseUrl(userInfoUri)
			.build();
	}
}
