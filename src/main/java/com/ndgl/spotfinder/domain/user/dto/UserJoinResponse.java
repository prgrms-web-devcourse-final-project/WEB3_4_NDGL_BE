package com.ndgl.spotfinder.domain.user.dto;

import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.User;

public record UserJoinResponse(
	String provide,
	String identify,
	String email,
	String nickName,
	String blogName
) {
	public static UserJoinResponse from(Oauth oauth) {
		User user = oauth.getUser();
		return new UserJoinResponse(
			oauth.getProvider().name(),
			oauth.getIdentify(),
			user.getEmail(),
			user.getNickName(),
			user.getBlogName()
		);
	}
}
