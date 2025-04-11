package com.ndgl.spotfinder.domain.auth.dto;


public record CheckAuthStatusResponseDto(
	Boolean isLoggedIn
) {
	public CheckAuthStatusResponseDto() {
		this(false); // 기본값 지정
	}
}
