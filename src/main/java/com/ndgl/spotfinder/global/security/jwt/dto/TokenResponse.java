package com.ndgl.spotfinder.global.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
	private final Integer code;
	private final String message;
}
