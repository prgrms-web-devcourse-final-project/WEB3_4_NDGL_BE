package com.ndgl.spotfinder.domain.user.dto;

import com.ndgl.spotfinder.domain.user.entity.User;

public record UserModifiedResponse(
	Integer code,
	String message,
	String nickName,
	String blogName
) {
	public static UserModifiedResponse success(Integer code, String message, User user) {
		return new UserModifiedResponse(
			code,
			message,
			user.getNickName(),
			user.getBlogName()
		);
	}
}
