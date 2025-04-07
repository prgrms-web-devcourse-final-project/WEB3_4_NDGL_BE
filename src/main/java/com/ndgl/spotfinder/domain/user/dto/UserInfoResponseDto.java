package com.ndgl.spotfinder.domain.user.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.user.entity.User;

public record UserInfoResponseDto(
	String nickname,
	String blogName,
	String email,
	LocalDateTime createdAt
) {
	public static UserInfoResponseDto from(User user) {
		return new UserInfoResponseDto(
			user.getNickName(),
			user.getBlogName(),
			user.getEmail(),
			user.getCreatedAt()
		);
	}
}
