package com.ndgl.spotfinder.global.rsdata;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
public class RsData<T> {
	@NonNull
	private final Integer code;
	@NonNull
	private final String message;

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
