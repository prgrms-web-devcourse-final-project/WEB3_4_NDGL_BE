package com.ndgl.spotfinder.global.security.reids.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 7)
public class RefreshToken {

	@Id
	private String email; // 소셜 플랫폼에서 제공한 인식 ID 정보

	private String token; //  refreshToken
}
