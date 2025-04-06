package com.ndgl.spotfinder.domain.user.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.ndgl.spotfinder.domain.user.dto.GoogleTokenResponseDTO;
import com.ndgl.spotfinder.domain.user.dto.UserLoginResponseDTO;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.OauthRepository;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;
import com.ndgl.spotfinder.global.security.jwt.CustomUserDetails;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class OauthService {
	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;

	@Value("${spring.security.oauth2.client.registration.google.client_secret}")
	private String googleClientSecret;

	@Value("${spring.security.oauth2.client.provider.google.token-uri}")
	private String tokenUri;

	@Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
	private String authorizationGrantType;

	@Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
	private String userInfoUri;

	@Value("${auth.header.prefix}")
	private String authHeaderPrefix;

	private final OauthRepository oauthRepository;
	private final UserRepository userRepository;
	private final TokenProvider tokenProvider;

	public OauthService(OauthRepository oauthRepository,
		UserRepository userRepository,
		TokenProvider tokenProvider) {
		this.oauthRepository = oauthRepository;
		this.userRepository = userRepository;
		this.tokenProvider = tokenProvider;
	}

	public UserLoginResponseDTO processGoogleLogin(
		Oauth.Provider provider,
		String code,
		String redirectUri,
		HttpServletResponse response) {
		// 1. 토큰 발급 : 구글
		String googleAccessToken = getAccessToken(provider, code, redirectUri);

		UserLoginResponseDTO googleUserInfo = getGoogleUserInfo(googleAccessToken);

		UserLoginResponseDTO googleUser = saveOrUpdateGoogleUser(googleUserInfo);

		if (googleUser.getCode() == HttpStatus.CREATED.value()) {
			// 회원가입 폼으로 이동할 유저이므로, 토큰 발급 X
			return googleUser;
		}

		//  유저 객체 생성
		User user = userRepository.findByEmail(googleUser.getEmail())
			.orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "NOT_FOUND"));

		CustomUserDetails customUserDetails = new CustomUserDetails(user);

		//  인증 객체 생성
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			customUserDetails, null, customUserDetails.getAuthorities()
		);

		// accessToken, refreshToken 생성 + 쿠키에 저장
		tokenProvider.createTokenAndSetCookies(authentication, response);

		return googleUser;

	}

	private String getAccessToken(
		Oauth.Provider provider,
		String code,
		String redirectUri) {
		String tokenRequestUrl;
		if (provider == Oauth.Provider.GOOGLE) {
			tokenRequestUrl = tokenUri;

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
			requestBody.add("grant_type", authorizationGrantType);
			requestBody.add("client_id", googleClientId);
			requestBody.add("client_secret", googleClientSecret);
			requestBody.add("code", code);
			requestBody.add("redirect_uri", redirectUri);

			HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity<GoogleTokenResponseDTO> tokenResponse = restTemplate.postForEntity(
				tokenRequestUrl, requestEntity, GoogleTokenResponseDTO.class
			);

			if (tokenResponse.getStatusCode() != HttpStatus.OK || tokenResponse.getBody() == null) {
				ErrorCode.UNAUTHORIZED.throwServiceException();
			}

			return tokenResponse.getBody().getAccessToken();
		} else {
			ErrorCode.NO_APPLIED_SOCIAL_PLATFORM.throwServiceException();
			return null;
		}
	}

	private UserLoginResponseDTO getGoogleUserInfo(String accessToken) {

		String userInfoUrl = userInfoUri;
		String prefix = authHeaderPrefix + " ";

		HttpHeaders headers = new HttpHeaders();
		//headers.add("Authorization", "Bearer " + accessToken);
		headers.add(HttpHeaders.AUTHORIZATION, prefix + accessToken);

		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<Map<String, Object>> userInfoResponse = restTemplate.exchange(
			userInfoUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
			}
		);

		if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "BAD_REQUEST"); // 500번으로 수정 후 OAUTH ERRORCODE 추가
		}

		Map<String, Object> responseMap = userInfoResponse.getBody();

		String identify = null;

		if (responseMap.containsKey("id")) {
			identify = responseMap.get("id").toString();
		} else if (responseMap.containsKey("sub")) {
			identify = responseMap.get("sub").toString();
		}

		if (identify == null) {
			throw new ServiceException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
		}

		String email = (String)responseMap.get("email");
		if (email == null || email.isEmpty()) {
			throw new ServiceException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");

		}

		return UserLoginResponseDTO.builder()
			.identify(identify)
			.email(email)
			.build();
	}

	private UserLoginResponseDTO saveOrUpdateGoogleUser(UserLoginResponseDTO userInfo) {
		String googleId = userInfo.getIdentify();
		String email = userInfo.getEmail();

		Optional<Oauth> existingOauthByIdentify = oauthRepository.findByIdentifyAndProvider(googleId,
			Oauth.Provider.GOOGLE);

		if (existingOauthByIdentify.isPresent()) {
			return UserLoginResponseDTO.builder()
				.message("OK")
				.code(HttpStatus.OK.value())
				.provider(Oauth.Provider.GOOGLE.name())
				.identify(googleId)
				.email(email)
				.build();
		}

		Optional<User> existingUser = userRepository.findByEmail(email);

		if (existingUser.isPresent()) {
			User nowUser = existingUser.get();

			Optional<Oauth> existingOauth = oauthRepository.findByUserAndProvider(nowUser, Oauth.Provider.GOOGLE);

			if (existingOauth.isEmpty()) {
				Oauth newOauth = Oauth.builder()
					.user(nowUser)
					.provider(Oauth.Provider.GOOGLE)
					.identify(googleId)
					.build();

				oauthRepository.save(newOauth);
			}

			return UserLoginResponseDTO.builder()
				.message("OK")
				.code(HttpStatus.OK.value())
				.provider(Oauth.Provider.GOOGLE.name())
				.identify(googleId)
				.email(email)
				.userId(nowUser.getId())
				.build();
		} else {
			return UserLoginResponseDTO.builder()
				.message("OK")
				.code(HttpStatus.CREATED.value())
				.provider(Oauth.Provider.GOOGLE.name())
				.identify(googleId)
				.email(email)
				.build();
		}
	}
}
