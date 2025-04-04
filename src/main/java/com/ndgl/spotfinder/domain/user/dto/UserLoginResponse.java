package com.ndgl.spotfinder.domain.user.dto;

import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.User;

public record UserLoginResponse(
	Integer code,
	Long userId,
	String provider,
	String identify,
	String email
) {
	public static UserLoginResponse from(Oauth oauth, Integer code) {
		User user = oauth.getUser();
		return new UserLoginResponse(
			code,
			user.getId(),
			oauth.getProvider().toString(),
			oauth.getIdentify(),
			user.getEmail()
		);
	}
}
