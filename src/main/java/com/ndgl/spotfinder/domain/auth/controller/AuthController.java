package com.ndgl.spotfinder.domain.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.auth.service.AuthService;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
			ErrorCode.MISSING_ACCESS_TOKEN.throwServiceException();
		}

		boolean isValid = authService.tokenStatusCheck(accessToken);

		if (!isValid) {
			ErrorCode.EXPIRED_ACCESS_TOKEN.throwServiceException();
		}

		return RsData.success(HttpStatus.OK, Map.of("isLoggedIn", true));

	}

	@PostMapping("/token/refresh")
	RsData<String> refreshAccessToken(
		HttpServletRequest request,
		HttpServletResponse response) {
		String accessToken = extractAccessTokenFromCookies(request);

		//  accessToken 유무 확인
		if (accessToken == null) {
			ErrorCode.MISSING_ACCESS_TOKEN.throwServiceException();
		}

		//  refreshToken이 redis에 있는지 확인
		String userId = tokenProvider.getEmail(accessToken);
		String refreshToken = authService.getRefreshTokenFromRedis(userId);

		//  새 accessToken 발급
		tokenProvider.createTokenAndSetCookiesByEmail(userId, response);

		return RsData.success(HttpStatus.OK);
	}

	private String extractAccessTokenFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("accessCookie".equals(cookie.getName())) { // ✅ Access Token 쿠키 이름
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
