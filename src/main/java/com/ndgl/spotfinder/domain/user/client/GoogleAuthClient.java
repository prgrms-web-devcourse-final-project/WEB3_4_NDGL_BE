package com.ndgl.spotfinder.domain.user.client;

import com.ndgl.spotfinder.domain.user.dto.GoogleTokenResponseDto;

public interface GoogleAuthClient {
	GoogleTokenResponseDto fetchToken(String code, String redirectUri);
}
