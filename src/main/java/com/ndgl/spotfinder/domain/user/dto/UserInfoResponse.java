package com.ndgl.spotfinder.domain.user.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.user.entity.User;

public record UserInfoResponse(
	String nickname,
	String blogName,
	String email,
	LocalDateTime createdAt
) {
	public static UserInfoResponse from(User user) {
		return new UserInfoResponse(
			user.getNickName(),
			user.getBlogName(),
			user.getEmail(),
			user.getCreatedAt()
		);
	}
}
