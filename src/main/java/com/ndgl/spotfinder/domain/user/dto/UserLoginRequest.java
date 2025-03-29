package com.ndgl.spotfinder.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginRequest {
	private String authorizationCode;
	private String provider;
}
