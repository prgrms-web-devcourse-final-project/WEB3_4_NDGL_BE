package com.ndgl.spotfinder.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginResponseDto {
	private final String message;
	private final Integer code;
	private final String provider;
	private final String identify;
	private final String email;
	private final Long userId;
}