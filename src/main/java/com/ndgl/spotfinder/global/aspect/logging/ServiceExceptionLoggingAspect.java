package com.ndgl.spotfinder.global.aspect.logging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.exception.ServiceException;
import com.ndgl.spotfinder.global.logging.context.RequestLogContext;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ServiceExceptionLoggingAspect {

	private final ObjectMapper objectMapper;
	private static final Logger requestLogger = LoggerFactory.getLogger("REQUEST_EXCEPTION_LOGGER");
	private static final Logger serviceLogger = LoggerFactory.getLogger("SERVICE_EXCEPTION_LOGGER");

	// Service 계층에서 발생한 예외 로깅
	@AfterThrowing(pointcut = "@within(org.springframework.stereotype.Service)", throwing = "ex")
	public void logGlobalException(JoinPoint joinPoint, Throwable ex) throws JsonProcessingException {
		logError(joinPoint, ex);
	}

	// 예상된 에러 :  Warn
	// 예상치 못한 에러 : Error
	public void logError(JoinPoint joinPoint, Throwable ex) throws JsonProcessingException {

		String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		int statusCode;
		String exceptionMessage;
		Logger selectedLogger = serviceLogger;

		boolean isWarning = true;

		if (ex instanceof ServiceException exception) {
			statusCode = exception.getCode().value();
			exceptionMessage = exception.getMessage();
		} else if (ex instanceof MethodArgumentNotValidException exception) {
			statusCode = HttpStatus.BAD_REQUEST.value();
			exceptionMessage = exception.getMessage();
		} else {
			isWarning = false;
			statusCode = 500;
			exceptionMessage = ex.getMessage();
		}

		// 서비스 예외 정보를 로그에 기록
		Map<String, Object> exceptionLogMap = new HashMap<>();
		exceptionLogMap.put("className", className);
		exceptionLogMap.put("methodName", methodName);
		exceptionLogMap.put("statusCode", statusCode);
		exceptionLogMap.put("exceptionMessage", exceptionMessage);

		// 쓰레드 로컬을 통해 요청 데이터 획득한 후 로그에 기록
		Map<String, Object> requestLogMap = RequestLogContext.get();
		if (requestLogMap != null) {
			addRequestInfoFromContext(exceptionLogMap);
			selectedLogger = requestLogger;
		}

		String logInfo = objectMapper.writeValueAsString(exceptionLogMap);

		if (isWarning) {
			selectedLogger.warn(logInfo);
			return;
		}
		selectedLogger.error(logInfo, ex);

	}

	private void addRequestInfoFromContext(Map<String, Object> targetMap) {
		Map<String, Object> context = RequestLogContext.get();
		if (context == null) return;

		for (String key : List.of("httpMethod", "requestURI", "httpBody", "ipAddress")) {
			if (context.containsKey(key)) {
				targetMap.put(key, context.get(key));
			}
		}
	}
}
