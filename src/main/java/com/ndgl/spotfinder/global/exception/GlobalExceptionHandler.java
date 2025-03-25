package com.ndgl.spotfinder.global.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ndgl.spotfinder.global.rsdata.RsData;

@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ServiceException.class)
	public RsData<Void> handleServiceException(ServiceException e) {
		return new RsData<>(e.getCode(), e.getMessage(), null);
	}
}
