package com.ndgl.spotfinder.domain.user.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.user.client.GoogleAuthClient;
import com.ndgl.spotfinder.domain.user.client.GoogleUserInfoClient;
import com.ndgl.spotfinder.domain.user.dto.GoogleTokenResponseDto;
import com.ndgl.spotfinder.domain.user.dto.RestClientDto;
import com.ndgl.spotfinder.domain.user.dto.UserLoginResponseDto;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.Provider;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.OauthRepository;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
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

	private final OauthRepository oauthRepository;
	private final UserRepository userRepository;
	private final TokenProvider tokenProvider;
	private final GoogleAuthClient googleAuthClient;
	private final GoogleUserInfoClient googleUserInfoClient;

	public OauthService(OauthRepository oauthRepository,
		UserRepository userRepository,
		TokenProvider tokenProvider,
		GoogleAuthClient googleAuthClient,
		GoogleUserInfoClient googleUserInfoClient
	) {
		this.oauthRepository = oauthRepository;
		this.userRepository = userRepository;
		this.tokenProvider = tokenProvider;
		this.googleAuthClient = googleAuthClient;
		this.googleUserInfoClient = googleUserInfoClient;
	}

	public UserLoginResponseDto processGoogleLogin(
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

		// accessToken, refreshToken 생성 + 쿠키에 저장
		tokenProvider.createTokenAndSetCookies(authentication, response);

		return googleUser;

	}

	private UserLoginResponseDto getGoogleUserInfo(String accessToken) {
		RestClientDto userInfo = googleUserInfoClient.fetchGoogleUserInfo(accessToken);

		return mapToUserLoginResponse(userInfo);
	}

	private UserLoginResponseDto mapToUserLoginResponse(RestClientDto userInfo) {
		return UserLoginResponseDto.builder()
			.identify(userInfo.id())
			.email(userInfo.email())
			.build();
	}

	private UserLoginResponseDto saveOrUpdateGoogleUser(UserLoginResponseDto userInfo) {
		String googleId = userInfo.getIdentify();
		String email = userInfo.getEmail();

		Optional<Oauth> existingOauthByIdentify = oauthRepository.findByIdentifyAndProvider(googleId,
			Provider.GOOGLE);

		if (existingOauthByIdentify.isPresent()) {
			User user = existingOauthByIdentify.get().getUser();
			if (!user.isResigned()) {
				return UserLoginResponseDto.builder()
					.message("OK")
					.code(HttpStatus.OK.value())
					.provider(Provider.GOOGLE.name())
					.identify(googleId)
					.email(email)
					.userId(existingOauthByIdentify.get().getId())
					.build();
			}
		}

		return UserLoginResponseDto.builder()
			.message("OK")
			.code(HttpStatus.CREATED.value())
			.provider(Provider.GOOGLE.name())
			.identify(googleId)
			.email(email)
			.build();
	}
}
