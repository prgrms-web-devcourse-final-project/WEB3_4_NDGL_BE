package com.ndgl.spotfinder.domain.auth.service;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;
import com.ndgl.spotfinder.global.security.redis.entity.RefreshToken;
import com.ndgl.spotfinder.global.security.redis.repository.RefreshTokenRepository;

@Service
public class AuthService {
	private final TokenProvider tokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthService(TokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository) {
		this.tokenProvider = tokenProvider;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public boolean tokenStatusCheck(String accessToken) {
		return tokenProvider.validateToken(accessToken);
	}

	public String getRefreshTokenFromRedis(String userId) {
		RefreshToken refreshToken = refreshTokenRepository.findById("refreshToken:" + userId)
			.orElseThrow(ErrorCode.MISSING_REFRESH_TOKEN::throwServiceException);

		return refreshToken.getToken();
	}
}
