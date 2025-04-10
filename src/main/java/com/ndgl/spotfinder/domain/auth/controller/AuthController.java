package com.ndgl.spotfinder.domain.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.auth.dto.CheckAuthStatusResponseDto;
import com.ndgl.spotfinder.domain.auth.service.AuthService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/status")
	public RsData<CheckAuthStatusResponseDto> checkAuthStatus(
		@CookieValue(value = "accessToken", required = false) String accessToken
	) {
		CheckAuthStatusResponseDto response = authService.tokenStatusCheck(accessToken);

		return RsData.success(HttpStatus.OK, response);
	}

	@PostMapping("/token/refresh")
	RsData<String> refreshAccessToken(
		HttpServletResponse response,
		@RequestBody Map<String, String> body
	) {
		String email = body.get("email");

		authService.tokenRefresh(email,response);

		return RsData.success(HttpStatus.OK);
	}
}
