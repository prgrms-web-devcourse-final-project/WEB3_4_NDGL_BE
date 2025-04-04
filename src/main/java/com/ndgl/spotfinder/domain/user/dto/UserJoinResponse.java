package com.ndgl.spotfinder.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserJoinResponse {
	private final String message;
	private final Integer code;
	private final String provide;
	private final String identify;
	private final String email;
	private final String nickName;
	private final String blogName;
}
