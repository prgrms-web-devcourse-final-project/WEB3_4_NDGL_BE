package com.ndgl.spotfinder.domain.user.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.user.entity.User;

public record UserInfoResponseDTO(
	String nickname,
	String blogName,
	String email,
	LocalDateTime createdAt
) {
	public static UserInfoResponseDTO from(User user) {
		return new UserInfoResponseDTO(
			user.getNickName(),
			user.getBlogName(),
			user.getEmail(),
			user.getCreatedAt()
		);
	}
}
