package com.ndgl.spotfinder.global.aspect.logging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.common.util.IpAddressUtil;
import com.ndgl.spotfinder.global.common.util.JsonUtil;
import com.ndgl.spotfinder.global.common.util.MaskingUtil;
import com.ndgl.spotfinder.global.common.util.RequestUtil;
import com.ndgl.spotfinder.global.logging.context.RequestLogContext;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ControllerLoggingAspect {

	private final ObjectMapper objectMapper;

	@Around("within(@org.springframework.web.bind.annotation.RestController *)")
	public Object logControllerRequests(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();

		try {
			// 요청 데이터 준비
			prepareRequestData(joinPoint);
			// 컨트롤러 메서드 실행
			Object result = joinPoint.proceed();

			// 응답 데이터와 함께 모든 정보 로깅
			Long elapsedTime = System.currentTimeMillis() - startTime;
			logResponseWithRequestData(result, elapsedTime);

			return result;
		} finally {
			RequestLogContext.clear();
		}
	}

	private void prepareRequestData(ProceedingJoinPoint joinPoint) {
		HttpServletRequest request = RequestUtil.getCurrentRequest();
		if (request == null) return;

		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String methodName = signature.getMethod().getName();
		String className = signature.getDeclaringType().getSimpleName();
		Object[] args = joinPoint.getArgs();

		// 중요 정보 마스킹 후 로깅
		Object[] maskedArgs = Arrays.stream(args)
			.filter(JsonUtil::isJsonSerializable)
			.map(MaskingUtil::maskSensitiveData)
			.toArray();

		Map<String, Object> requestLogMap = new HashMap<>();
		requestLogMap.put("className", className);
		requestLogMap.put("methodName", methodName);
		requestLogMap.put("httpMethod", request.getMethod());
		requestLogMap.put("requestURI", request.getRequestURI());
		requestLogMap.put("httpBody", maskedArgs);
		requestLogMap.put("ipAddress", IpAddressUtil.getClientIp(request));

		RequestLogContext.set(requestLogMap);
	}

	private void logResponseWithRequestData(Object result, Long elapsedTime) throws Throwable{
		Map<String, Object> requestLogMap = RequestLogContext.get();
		if (requestLogMap == null) {
			requestLogMap = new HashMap<>();
		}

		int statusCode = 200;
		if (result instanceof RsData<?> response) {
			statusCode = response.getCode();
		} else if (result instanceof ResponseEntity<?> response) {
			statusCode = response.getStatusCode().value();
		}

		Map<String, Object> completeLogMap = new HashMap<>(requestLogMap);
		completeLogMap.put("statusCode", statusCode);
		completeLogMap.put("elapsedTime", elapsedTime);

		log.info(objectMapper.writeValueAsString(completeLogMap));

	}
}
