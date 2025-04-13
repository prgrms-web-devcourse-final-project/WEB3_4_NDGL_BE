package com.ndgl.spotfinder.global.exception;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public RsData<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		return RsData.error(ErrorCode.UNREADABLE_REQUEST_PAYLOAD);
	}

	@ExceptionHandler(Exception.class)
	public RsData<Void> handleGenericException(Exception e, HttpServletResponse response) {
		if (response.isCommitted()) {
			log.warn("응답이 이미 커밋된 상태에서 예외 발생: {}", e.getMessage());
			return null; // 이미 응답했으니 더 이상 처리하지 않음
		}

		log.error("Unhandled exception", e);
		return new RsData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다.", null);
	}
}