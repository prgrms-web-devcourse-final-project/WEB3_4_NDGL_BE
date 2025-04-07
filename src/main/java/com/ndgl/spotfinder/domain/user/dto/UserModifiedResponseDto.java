package com.ndgl.spotfinder.domain.user.dto;

import com.ndgl.spotfinder.domain.user.entity.User;

public record UserModifiedResponseDto(
	Integer code,
	String message,
	String nickName,
	String blogName
) {
	public static UserModifiedResponseDto success(Integer code, String message, User user) {
		return new UserModifiedResponseDto(
			code,
			message,
			user.getNickName(),
			user.getBlogName()
		);
	}
}
