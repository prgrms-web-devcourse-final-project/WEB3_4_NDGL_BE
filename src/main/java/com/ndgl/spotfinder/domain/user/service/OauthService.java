package com.ndgl.spotfinder.domain.user.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ndgl.spotfinder.domain.user.client.GoogleAuthClient;
import com.ndgl.spotfinder.domain.user.dto.GoogleTokenResponseDto;
import com.ndgl.spotfinder.domain.user.dto.UserLoginResponseDto;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.Provider;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.OauthRepository;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;
import com.ndgl.spotfinder.global.security.jwt.CustomUserDetails;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OauthService {
	@Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
	private String userInfoUri;

	@Value("${auth.header.prefix}")
	private String authHeaderPrefix;

	private final OauthRepository oauthRepository;
	private final UserRepository userRepository;
	private final TokenProvider tokenProvider;
	private final GoogleAuthClient googleAuthClient;

	public OauthService(OauthRepository oauthRepository,
		UserRepository userRepository,
		TokenProvider tokenProvider,
		GoogleAuthClient googleAuthClient) {
		this.oauthRepository = oauthRepository;
		this.userRepository = userRepository;
		this.tokenProvider = tokenProvider;
		this.googleAuthClient = googleAuthClient;
	}

	public UserLoginResponseDto processGoogleLogin(
		Provider provider,
		String code,
		String redirectUri,
		HttpServletResponse response) {
		// 1. 토큰 발급 : 구글
		GoogleTokenResponseDto googleToken = googleAuthClient.fetchToken(code, redirectUri);

		//  2.  구글 유저 정보 조회
		UserLoginResponseDto googleUserInfo = getGoogleUserInfo(googleToken.getAccessToken());

		//  3.  유저 저장 또는 회원가입 유도
		UserLoginResponseDto googleUser = saveOrUpdateGoogleUser(googleUserInfo);

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

		log.info("Authentication: {}", authentication);

		// accessToken, refreshToken 생성 + 쿠키에 저장
		tokenProvider.createTokenAndSetCookies(authentication, response);

		return googleUser;

	}

	private UserLoginResponseDto getGoogleUserInfo(String accessToken) {

		String userInfoUrl = userInfoUri;
		String prefix = authHeaderPrefix + " ";

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, prefix + accessToken);

		HttpEntity<?> entity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<Map<String, Object>> userInfoResponse = restTemplate.exchange(
			userInfoUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
			}
		);

		if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
			ErrorCode.SERVER_ERROR.throwServiceException();
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

		return UserLoginResponseDto.builder()
			.identify(identify)
			.email(email)
			.build();
	}

	private UserLoginResponseDto saveOrUpdateGoogleUser(UserLoginResponseDto userInfo) {
		String googleId = userInfo.getIdentify();
		String email = userInfo.getEmail();

		Optional<Oauth> existingOauthByIdentify = oauthRepository.findByIdentifyAndProvider(googleId,
			Provider.GOOGLE);

		if (existingOauthByIdentify.isPresent()) {
			return UserLoginResponseDto.builder()
				.message("OK")
				.code(HttpStatus.OK.value())
				.provider(Provider.GOOGLE.name())
				.identify(googleId)
				.email(email)
				.userId(existingOauthByIdentify.get().getId())
				.build();
		}

		Optional<User> existingUser = userRepository.findByEmail(email);

		if (existingUser.isPresent()) {
			User nowUser = existingUser.get();

			Optional<Oauth> existingOauth = oauthRepository.findByUserAndProvider(nowUser, Provider.GOOGLE);

			if (existingOauth.isEmpty()) {
				Oauth newOauth = Oauth.builder()
					.user(nowUser)
					.provider(Provider.GOOGLE)
					.identify(googleId)
					.build();

				oauthRepository.save(newOauth);
			}

			return UserLoginResponseDto.builder()
				.message("OK")
				.code(HttpStatus.OK.value())
				.provider(Provider.GOOGLE.name())
				.identify(googleId)
				.email(email)
				.userId(existingOauth.get().getId())
				.build();
		} else {
			return UserLoginResponseDto.builder()
				.message("OK")
				.code(HttpStatus.CREATED.value())
				.provider(Provider.GOOGLE.name())
				.identify(googleId)
				.email(email)
				.build();
		}
	}
}
