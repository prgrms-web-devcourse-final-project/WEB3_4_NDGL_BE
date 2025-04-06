package com.ndgl.spotfinder.domain.user.dto;

import com.ndgl.spotfinder.domain.user.entity.User;

public record UserModifiedResponseDTO(
	Integer code,
	String message,
	String nickName,
	String blogName
) {
	public static UserModifiedResponseDTO success(Integer code, String message, User user) {
		return new UserModifiedResponseDTO(
			code,
			message,
			user.getNickName(),
			user.getBlogName()
		);
	}
}
