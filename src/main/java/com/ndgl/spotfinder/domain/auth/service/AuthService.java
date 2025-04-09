package com.ndgl.spotfinder.domain.auth.service;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;
import com.ndgl.spotfinder.global.security.redis.entity.RefreshToken;
import com.ndgl.spotfinder.global.security.redis.repository.RefreshTokenRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {
	private final TokenProvider tokenProvider;
	private final RedisTemplate<String, String> redisTemplate;

	public AuthService(TokenProvider tokenProvider,
		RedisTemplate<String, String> redisTemplate) {
		this.tokenProvider = tokenProvider;
		this.redisTemplate = redisTemplate;
	}

	public boolean tokenStatusCheck(String accessToken) {
		return tokenProvider.validateToken(accessToken);
	}

	public String getRefreshTokenFromRedis(String userId) {
		String key = "refreshToken:" + userId;
		Object refreshToken = redisTemplate.opsForHash().get(key, "token");

		if (refreshToken == null) {
			ErrorCode.EXPIRED_REFRESH_TOKEN.throwServiceException();
		}

		return refreshToken.toString();
	}
}
