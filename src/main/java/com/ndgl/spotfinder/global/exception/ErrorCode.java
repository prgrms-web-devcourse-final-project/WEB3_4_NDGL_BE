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
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
	CONFLICTED_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 닉네임 입니다."),
	CONFLICTED_BLOG_NAME(HttpStatus.CONFLICT, "이미 사용중인 블로그 명 입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "대상 유저가 없습니다."),
	NO_APPLIED_SOCIAL_PLATFORM(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 플랫폼입니다"),

	POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "작성자만 수정 또는 삭제할 수 있습니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 포스트입니다."),

	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."),
	NOT_FOUND_IN_POST(HttpStatus.BAD_REQUEST, "해당 포스트의 댓글이 아닙니다."),

	// LIKE
	UNSUPPORTED_TARGET_TYPE(HttpStatus.NOT_FOUND, "지원하지 않는 타겟 유형입니다");

	private final HttpStatus httpStatus;
	private final String message;

	public ServiceException throwServiceException() {
		throw new ServiceException(httpStatus, message);
	}

	public ServiceException throwServiceException(Throwable cause) {
		throw new ServiceException(httpStatus, message, cause);
	}
}
