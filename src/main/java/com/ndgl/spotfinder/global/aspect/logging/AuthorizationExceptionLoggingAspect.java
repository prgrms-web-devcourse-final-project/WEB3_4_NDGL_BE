package com.ndgl.spotfinder.global.aspect.logging;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.common.util.IpAddressUtil;
import com.ndgl.spotfinder.global.common.util.MaskingUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationExceptionLoggingAspect {

	private final ObjectMapper objectMapper;

	// 스프링 시큐리티 Authorization 실패 : 403 예외
	@After("execution(* com.ndgl.spotfinder.global.security.handler.CustomAccessDeniedHandler.handle(..))")
	public void logAccessDeniedException(JoinPoint joinPoint) throws Throwable {

		Object[] args = joinPoint.getArgs();
		HttpServletRequest request = (HttpServletRequest) args[0];
		AccessDeniedException accessDeniedException = (AccessDeniedException) args[2];

		Map<String, Object> logMap = new HashMap<>();
		logMap.put("requestURI", request.getRequestURI());
		logMap.put("user", MaskingUtil.maskEmail(SecurityContextHolder.getContext().getAuthentication().getName()));
		logMap.put("ipAddress", IpAddressUtil.getClientIp(request));
		logMap.put("userAgent", request.getHeader("User-Agent"));

		log.error(objectMapper.writeValueAsString(logMap), accessDeniedException);

	}
}
