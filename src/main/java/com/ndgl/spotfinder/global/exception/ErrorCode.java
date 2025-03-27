package com.ndgl.spotfinder.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Getter
@AllArgsConstructor
@Slf4j
public enum ErrorCode {
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 포스트입니다.");
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

	// S3
	S3_PRESIGNED_GENERATION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 실패"),
	S3_OBJECT_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3 객체 삭제 실패"),
	S3_OBJECT_ACCESS_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3 객체 조회 실패"),
	S3_OBJECT_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드 실패"),
	S3_INVALID_URL(HttpStatus.INTERNAL_SERVER_ERROR, "옳지 않은 URL");

	private final HttpStatus httpStatus;
	private final String message;

	public ServiceException throwServiceException() {
		throw new ServiceException(httpStatus, message);
	}

	public S3Exception throwS3Exception(Throwable cause) {
		throw new CustomS3Exception(httpStatus, message, cause);
	}

	public ServiceException throwServiceException(Throwable cause) {
		throw new ServiceException(httpStatus, message, cause);
	}
}
