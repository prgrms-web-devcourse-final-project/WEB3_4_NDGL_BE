package com.ndgl.spotfinder.domain.user.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ndgl.spotfinder.domain.user.dto.RestClientDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleUserInfoClient {

	private final RestClient googleUserInfoRestClient;

	public RestClientDto fetchGoogleUserInfo(String accessToken) {
		return googleUserInfoRestClient.get()
			.uri("/")
			.headers(headers -> headers.setBearerAuth(accessToken))
			.retrieve()
			.body(RestClientDto.class);
	}
}
