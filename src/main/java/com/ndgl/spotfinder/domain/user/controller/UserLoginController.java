package com.ndgl.spotfinder.domain.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.user.service.UserLoginService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/v1/user")
public class UserLoginController {

	private final UserLoginService userLoginService;

	public UserLoginController(UserLoginService userLoginService) {
		this.userLoginService = userLoginService;
	}

	@GetMapping("/login")
	public ResponseEntity<Void> login(HttpSession session) {
		String redirectUrl = userLoginService.createGoogleLoginUrl(session);

		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, redirectUrl).build();
	}
}
