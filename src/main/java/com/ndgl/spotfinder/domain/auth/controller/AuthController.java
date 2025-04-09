package com.ndgl.spotfinder.domain.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.auth.service.AuthService;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final TokenProvider tokenProvider;

	public AuthController(AuthService authService, TokenProvider tokenProvider) {
		this.authService = authService;
		this.tokenProvider = tokenProvider;
	}

	@GetMapping("/status")
	public RsData<Map<String, Boolean>> checkAuthStatus(
		@CookieValue(value = "accessToken", required = false) String accessToken
	) {
		if (accessToken == null) {
			return RsData.success(HttpStatus.OK, Map.of("isLoggedIn", false));
		}

		boolean isValid = authService.tokenStatusCheck(accessToken);

		if (!isValid) {
			return RsData.success(HttpStatus.OK, Map.of("isLoggedIn", false));
		}

		return RsData.success(HttpStatus.OK, Map.of("isLoggedIn", true));
	}

	@PostMapping("/token/refresh")
	RsData<String> refreshAccessToken(
		HttpServletResponse response,
		@RequestBody Map<String, String> body
	) {
		String email = body.get("email");

		try {
			// 대상 유저의 email 정보를 가지고 redis에 refreshToken이 있나 확인
			authService.getRefreshTokenFromRedis(email);

			//  새 accessToken 발급
			tokenProvider.createTokenAndSetCookiesByEmail(email, response);

		} catch (ServiceException e) {
			ErrorCode.EXPIRED_ACCESS_TOKEN.throwServiceException();
		}

		return RsData.success(HttpStatus.OK);
	}
}
