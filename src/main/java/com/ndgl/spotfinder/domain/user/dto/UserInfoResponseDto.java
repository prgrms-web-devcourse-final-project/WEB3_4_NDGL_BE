package com.ndgl.spotfinder.domain.user.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserInfoResponseDto(
	@Schema(description = "유저의 닉네임.", examples = "testman001")
	String nickname,

	@Schema(description = "유저의 블로그 명.", examples = "testblog001")
	String blogName,

	@Schema(description = "유저의 이메일 정보.", examples = "testman001@test.com")
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
