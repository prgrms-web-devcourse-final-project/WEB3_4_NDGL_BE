package com.ndgl.spotfinder.domain.auth.service;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

@Service
public class AuthService {
	private final TokenProvider tokenProvider;

	public AuthService(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	public boolean tokenStatusCheck(String accessToken) {
		return tokenProvider.validateToken(accessToken);
	}
}
