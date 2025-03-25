package com.ndgl.spotfinder.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
	private final HttpStatus code;
	private final String message;

	public ServiceException(HttpStatus code, String message) {
		super(code + " : " + message);
		this.code = code;
		this.message = message;
	}

	public ServiceException(HttpStatus code, String message, Throwable cause) {
		super(code + " : " + message, cause);
		this.code = code;
		this.message = message;
	}
}
