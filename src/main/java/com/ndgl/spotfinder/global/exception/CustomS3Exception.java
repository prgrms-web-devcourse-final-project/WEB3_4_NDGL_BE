package com.ndgl.spotfinder.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomS3Exception extends RuntimeException {
    private final HttpStatus resultCode;
    private final String msg;

    public CustomS3Exception(HttpStatus resultCode, String msg, Throwable cause) {
        super(resultCode + ":" + msg, cause);
        this.resultCode = resultCode;
        this.msg = msg;
    }
}