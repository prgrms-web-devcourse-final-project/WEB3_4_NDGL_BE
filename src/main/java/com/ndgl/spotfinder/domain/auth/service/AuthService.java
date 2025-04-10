package com.ndgl.spotfinder.domain.auth.service;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;
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

	public void tokenRefresh(String email, HttpServletResponse response) {
		try {
			// 대상 유저의 email 정보를 가지고 redis에 refreshToken이 있나 확인
			getRefreshTokenFromRedis(email,response);

		} catch (ServiceException e) {
			ErrorCode.EXPIRED_ACCESS_TOKEN.throwServiceException();
		}
	}

	public void getRefreshTokenFromRedis(
		String email,
		HttpServletResponse response) {
		String key = "refreshToken:" + email;
		Object refreshToken_obj = redisTemplate.opsForHash().get(key, "token");

		if (refreshToken_obj == null) {
			ErrorCode.MISSING_REFRESH_TOKEN.throwServiceException();
		}

		String refreshToken = refreshToken_obj.toString();

		boolean isValid = tokenStatusCheck(refreshToken);

		//  refreshToken 체크. 만약 refreshToken이 만료 되었다면 갱신 처리 진행.
		if (!isValid) {
			String authorities = tokenProvider.extractAuthoritiesEvenIfExpired(refreshToken);
			tokenProvider.createRefreshToken(email,authorities);

			log.info("Refresh token 갱신 완료");
		}

		//  새 accessToken 발급
		tokenProvider.refreshAccessToken(email, response);
	}
}
