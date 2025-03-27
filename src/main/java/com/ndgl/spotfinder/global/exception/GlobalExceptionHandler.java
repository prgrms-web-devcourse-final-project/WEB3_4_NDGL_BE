package com.ndgl.spotfinder.global.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ndgl.spotfinder.global.rsdata.RsData;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ServiceException.class)
	public RsData<Void> handleServiceException(ServiceException e) {
		return new RsData<>(e.getCode().value(), e.getMessage(), null);
	}
}
