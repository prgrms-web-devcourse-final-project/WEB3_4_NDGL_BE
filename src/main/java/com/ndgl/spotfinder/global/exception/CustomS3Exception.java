package com.ndgl.spotfinder.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomS3Exception extends RuntimeException {
    private final HttpStatus code;
    private final String message;

    public CustomS3Exception(HttpStatus code, String message, Throwable cause) {
        super(code + ":" + message, cause);
        this.code = code;
        this.message = message;
    }
}