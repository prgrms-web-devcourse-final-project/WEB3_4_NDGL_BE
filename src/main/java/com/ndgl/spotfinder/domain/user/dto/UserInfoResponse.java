package com.ndgl.spotfinder.domain.user.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
	private final String message;
	private final Integer code;
	private final String nickname;
	private final String blogName;
	private final String email;
	private final LocalDateTime createdAt;
}
