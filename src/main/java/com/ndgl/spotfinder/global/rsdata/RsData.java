package com.ndgl.spotfinder.global.rsdata;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
public class RsData<T> {
	@NonNull
	private final HttpStatus code;
	@NonNull
	private final String message;

	private final T data;

	@JsonIgnore
	public boolean isSuccess() {
		return !code.isError();
	}

	public static <T> RsData<T> success(HttpStatus resultCode, T data) {
		return new RsData<>(resultCode, "OK", data);
	}
}
