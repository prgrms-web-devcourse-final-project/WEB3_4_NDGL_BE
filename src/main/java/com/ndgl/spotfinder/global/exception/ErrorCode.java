package com.ndgl.spotfinder.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum ErrorCode {
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

	private final HttpStatus httpStatus;
	private final String message;

	public ServiceException throwServiceException() {
		throw new ServiceException(httpStatus, message);
	}

	public ServiceException throwServiceException(Throwable cause) {
		throw new ServiceException(httpStatus, message, cause);
	}
}
