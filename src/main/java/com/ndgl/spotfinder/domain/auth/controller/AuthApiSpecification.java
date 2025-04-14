package com.ndgl.spotfinder.domain.auth.controller;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.ndgl.spotfinder.domain.auth.dto.CheckAuthStatusResponseDto;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "권한(Auth)")
public interface AuthApiSpecification {

	@Operation(
		summary = "JWT 토큰 만료시간 체크, ",
		description = "로그인 된 상황에서, cookie 통해 토큰을 받으면, 토큰의 상태를 체크, 만료 된 토큰이면 갱신 처리 진행.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	@GetMapping("/status")
	RsData<CheckAuthStatusResponseDto> checkAuthStatus(
		@CookieValue(value = "accessToken", required = false) String accessToken,
		@CookieValue(value = "refreshToken", required = false) String refreshToken,
		HttpServletResponse response
	);

	@Operation(
		summary = "JWT 토큰 갱신, ",
		description = "입력 받은 accessToken 갱신",
		security = {@SecurityRequirement(name = "JWT")}
	)
	@PostMapping("/token/refresh")
	RsData<String> refreshAccessToken(
		HttpServletResponse response,
		@CookieValue(value = "accessToken", required = false) String accessToken,
		@CookieValue(value = "refreshToken", required = false) String refreshToken
	);

}
