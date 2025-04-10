package com.ndgl.spotfinder.domain.auth.service;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;
import com.ndgl.spotfinder.global.security.redis.entity.RefreshToken;
import com.ndgl.spotfinder.global.security.redis.repository.RefreshTokenRepository;

import jakarta.servlet.http.HttpServletResponse;
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

	public boolean tokenStatusCheck(String token) {
		return tokenProvider.validateToken(token);
	}

	public String getRefreshTokenFromRedis(
		String userId,
		HttpServletResponse response) {
		String key = "refreshToken:" + userId;
		Object refreshToken_obj = redisTemplate.opsForHash().get(key, "token");

		if (refreshToken_obj == null) {
			ErrorCode.MISSING_REFRESH_TOKEN.throwServiceException();
		}

		String refreshToken = refreshToken_obj.toString();

		boolean isValid = tokenStatusCheck(refreshToken);

		//  refreshToken 체크. 만약 refreshToken이 만료 되었다면 갱신 처리 진행.
		if (!isValid) {
			String authorities = tokenProvider.extractAuthoritiesEvenIfExpired(refreshToken);
			refreshToken = tokenProvider.createRefreshToken(userId,authorities);

			log.info("Refresh token 갱신 완료");
		}

		//  새 accessToken 발급
		tokenProvider.refreshAccessToken(userId, response);

		return refreshToken;
	}
}
