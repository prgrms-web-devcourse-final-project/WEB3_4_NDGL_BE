package com.ndgl.spotfinder.domain.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ndgl.spotfinder.domain.user.dto.GoogleTokenRequestDto;
import com.ndgl.spotfinder.domain.user.dto.GoogleTokenResponseDto;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestTemplateGoogleAuthClient implements GoogleAuthClient {
	private final RestTemplate restTemplate;

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
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		GoogleTokenRequestDto requestBody = new GoogleTokenRequestDto(
			authorizationGrantType,
			googleClientId,
			googleClientSecret,
			code,
			redirectUri
		);

		String body = String.format(
			"grant_type=%s&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
			requestBody.grantType(),
			requestBody.clientId(),
			requestBody.clientSecret(),
			requestBody.code(),
			requestBody.redirectUri()
		);

		HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<GoogleTokenResponseDto> responseEntity =
				restTemplate.postForEntity(
					tokenUri,
					requestEntity,
					GoogleTokenResponseDto.class
				);

			//  응답상태 및 본문 null 체크
			if (
				responseEntity.getStatusCode() != HttpStatus.OK ||
					responseEntity.getBody() == null) {
				ErrorCode.UNAUTHORIZED.throwServiceException();
			}
			return responseEntity.getBody();

		} catch (RestClientException e) {
			throw new RuntimeException(e);
		}
	}
}
