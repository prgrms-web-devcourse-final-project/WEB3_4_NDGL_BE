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

	ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 관리자입니다."),

	CONFLICTED_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 닉네임 입니다."),
	CONFLICTED_BLOG_NAME(HttpStatus.CONFLICT, "이미 사용중인 블로그 명 입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "대상 유저가 없습니다."),
	NO_APPLIED_SOCIAL_PLATFORM(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 플랫폼입니다"),

	POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "작성자만 수정 또는 삭제할 수 있습니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 포스트입니다."),

	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."),
	NOT_FOUND_IN_POST(HttpStatus.BAD_REQUEST, "해당 포스트의 댓글이 아닙니다."),

	// LIKE
	UNSUPPORTED_TARGET_TYPE(HttpStatus.NOT_FOUND, "지원하지 않는 타겟 유형입니다"),

	// REPORT
	REPORTER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신고자입니다."),
	REPORTED_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신고 대상자입니다."),
	BAN_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "제재할 사용자가 존재하지 않습니다."),
	REPORTED_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "신고한 글이 존재하지 않습니다."),
	REPORTED_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고한 댓글이 존재하지 않습니다."),
	POST_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 포스트 신고입니다."),
	COMMENT_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글 신고입니다."),
	EMPTY_POST_REPORT_SLICE(HttpStatus.NOT_FOUND, "조회된 포스트 신고 데이터가 없습니다."),
	EMPTY_COMMENT_REPORT_SLICE(HttpStatus.NOT_FOUND, "조회된 댓글 신고 데이터가 없습니다."),
	INVALID_BAN_DURATION(HttpStatus.BAD_REQUEST, "유효하지 않은 제재 일자 옵션입니다."),

	MISSING_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh Token 이 유효하지 않습니다."),
	EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh Token 이 만료되었습니다."),
	MISSING_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token 이 유효하지 않습니다."),
	EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token 이 만료되었습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	public ServiceException throwServiceException() {
		throw new ServiceException(httpStatus, message);
	}

	public ServiceException throwServiceException(Throwable cause) {
		throw new ServiceException(httpStatus, message, cause);
	}
}
