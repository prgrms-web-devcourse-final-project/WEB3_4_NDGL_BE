package com.ndgl.spotfinder.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ndgl.spotfinder.global.rsdata.RsData;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ServiceException.class)
	public RsData<Void> handleServiceException(ServiceException e) {
		return new RsData<>(e.getCode().value(), e.getMessage(), null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public RsData<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		return new RsData<>(HttpStatus.BAD_REQUEST.value(), "잘못된 입력 값입니다.", null);
	}
}
