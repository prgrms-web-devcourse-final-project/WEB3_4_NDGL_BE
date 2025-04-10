package com.ndgl.spotfinder.domain.auth.service;



import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.auth.dto.CheckAuthStatusResponseDto;

import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

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

	//  토큰의 유효성 check
	public boolean validCheckToken(String token) {
		return tokenProvider.validateToken(token);
	}

	public CheckAuthStatusResponseDto statusCheck(String accessToken,String refreshToken,HttpServletResponse response) {
		//  accessToken이 없는경우.
		if (accessToken == null) {
			return new CheckAuthStatusResponseDto(false);
		}

		boolean isValid = validCheckToken(accessToken);

		// accessToken이 유효하지 않는 경우.
		if (!isValid) {
			refreshAccessToken(refreshToken,accessToken,response);
		}

		return new CheckAuthStatusResponseDto(true);
	}

	public void refreshAccessToken (
		String accessToken,
		String refreshToken,
		HttpServletResponse response)
	{
		tokenProvider.refreshAccessToken(refreshToken, accessToken, response);
	}

}
