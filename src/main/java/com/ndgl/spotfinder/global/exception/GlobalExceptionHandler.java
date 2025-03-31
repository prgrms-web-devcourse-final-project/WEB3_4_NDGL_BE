package com.ndgl.spotfinder.global.exception;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
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
		String errors = e.getBindingResult()
			.getAllErrors()
			.stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.joining("\n"));

		return new RsData<>(HttpStatus.BAD_REQUEST.value(), errors, null);
	}
}
