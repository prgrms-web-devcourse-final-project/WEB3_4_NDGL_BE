package com.ndgl.spotfinder.global.aspect.logging;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.common.util.IpAddressUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminLoginFailLoggingAspect {

	private final ObjectMapper objectMapper;

	// 스프링 시큐리티 Admin form 로그인 실패 : 401 예외
	@After("execution(* com.ndgl.spotfinder.global.security.handler.CustomAuthenticationFailureHandler.onAuthenticationFailure(..))")
	public void logAdminLoginAuthenticationException(JoinPoint joinPoint) throws Throwable {

		Object[] args = joinPoint.getArgs();

		HttpServletRequest request = (HttpServletRequest) args[0];
		AuthenticationException authException = (AuthenticationException) args[2];

		Map<String, Object> logMap = new HashMap<>();
		logMap.put("ipAddress", IpAddressUtil.getClientIp(request));
		logMap.put("userAgent", request.getHeader("User-Agent"));

		log.error(objectMapper.writeValueAsString(logMap), authException);

	}
}
