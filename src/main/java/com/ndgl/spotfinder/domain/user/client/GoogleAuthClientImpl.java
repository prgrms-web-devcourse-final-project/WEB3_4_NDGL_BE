package com.ndgl.spotfinder.domain.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ndgl.spotfinder.domain.user.dto.GoogleTokenResponseDto;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleAuthClientImpl implements GoogleAuthClient {

	@Value("${spring.security.oauth2.client.provider.google.token-uri}")
	private String tokenUri;

	@Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
	private String authorizationGrantType;

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;

	@Value("${spring.security.oauth2.client.registration.google.client_secret}")
	private String googleClientSecret;

	@Override
	public GoogleTokenResponseDto fetchToken(String code, String redirectUri) {

		String body = String.format(
			"grant_type=%s&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
			authorizationGrantType,
			googleClientId,
			googleClientSecret,
			code,
			redirectUri
		);

		try {
			RestClient restClient = RestClient.builder()
				.baseUrl(tokenUri)
				.defaultHeaders(httpHeaders -> {
					httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
				})
				.build();

			GoogleTokenResponseDto response = restClient.post()
				.body(body)
				.retrieve()
				.body(GoogleTokenResponseDto.class);

			if (response == null) {
				ErrorCode.UNAUTHORIZED.throwServiceException();
			}
			return response;

		} catch (RestClientException e) {
			throw new RuntimeException(e);
		}
	}
}
