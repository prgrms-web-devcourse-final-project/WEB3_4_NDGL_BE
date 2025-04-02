package com.ndgl.spotfinder.global.rsdata;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "응답 데이터 포맷")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
public class RsData<T> {
	@Schema(description = "응답 코드", example = "200")
	@NonNull
	private final Integer code;

	@Schema(description = "응답 메시지", example = "OK")
	@NonNull
	private final String message;

	@Schema(description = "응답 데이터")
	private final T data;

	public static <T> RsData<T> success(HttpStatus resultCode, T data) {
		return new RsData<>(resultCode.value(), "OK", data);
	}

	public static <T> RsData<T> success(HttpStatus resultCode) {
		return new RsData<>(resultCode.value(), "OK", null);
	}

	public static <T> RsData<T> error(ErrorCode error) {
		return new RsData<>(error.getHttpStatus().value(), error.getMessage(), null);
	}
}
