package com.ndgl.spotfinder.domain.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ndgl.spotfinder.domain.user.dto.GoogleTokenResponseDto;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthClientImpl implements GoogleAuthClient {

	private final RestClient googleRestClient;

	@Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
	private String authorizationGrantType;

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;

	@Value("${spring.security.oauth2.client.registration.google.client_secret}")
	private String googleClientSecret;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String googleClientSecret2;

	@Override
	public GoogleTokenResponseDto fetchToken(String code, String redirectUri) {

		log.info("googleClientSecret = {}", googleClientSecret);
		log.info("googleClientSecret2 = {}", googleClientSecret2);
		log.info("rest client redirectUri = {}", redirectUri);

		// String body = String.format(
		// 	"grant_type=%s"
		// 		+ "&client_id=%s"
		// 		+ "&client_secret=%s"
		// 		+ "&code=%s"
		// 		+ "&redirect_uri=%s",
		// 	authorizationGrantType,
		// 	googleClientId,
		// 	googleClientSecret,
		// 	code,
		// 	redirectUri
		// );

		String body = new StringBuilder()
			.append("grant_type=")
			.append(authorizationGrantType)
			.append("&client_id=")
			.append(googleClientId)
			.append("&client_secret=")
			.append(googleClientSecret)
			.append("&code=")
			.append(code)
			.append("&redirect_uri=")
			.append(redirectUri)
			.toString();

		try {
			GoogleTokenResponseDto response = googleRestClient.post()
				.body(body)
				.retrieve()
				.body(GoogleTokenResponseDto.class);

			if (response == null) {
				ErrorCode.UNAUTHORIZED.throwServiceException();
			}
			return response;

		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
				ErrorCode.INVALID_OAUTH_CODE.throwServiceException(e);
			}
			throw e;

		} catch (RestClientException e) {
			ErrorCode.SERVER_ERROR.throwServiceException(e);
		}

		return null;
	}
}
