package com.ndgl.spotfinder.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum ErrorCode {
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	CONFLICTED_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 닉네임 입니다."),
	CONFLICTED_BLOG_NAME(HttpStatus.CONFLICT, "이미 사용중인 블로그 명 입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "대상 유저가 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	public ServiceException throwServiceException() {
		throw new ServiceException(httpStatus, message);
	}

	public ServiceException throwServiceException(Throwable cause) {
		throw new ServiceException(httpStatus, message, cause);
	}
}
