package com.ndgl.spotfinder.global.aspect.logging;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
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
public class AdminAuthLoggingAspect {

	private final ObjectMapper objectMapper;

	// 관리자 로그인 성공
	@After("execution(* com.ndgl.spotfinder.global.security.handler.CustomAuthenticationSuccessHandler.onAuthenticationSuccess(..))")
	public void logAdminLoginSuccess(JoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		HttpServletRequest request = (HttpServletRequest) args[0];
		Authentication authentication = (Authentication) args[2];

		Map<String, Object> logMap = new HashMap<>();
		logMap.put("event", "Login");
		logMap.put("user", authentication.getName());
		logMap.put("ipAddress", IpAddressUtil.getClientIp(request));

		String jsonLog = objectMapper.writeValueAsString(logMap);
		log.info(jsonLog);
	}

	// 관리자 로그아웃 성공
	@Around("execution(* com.ndgl.spotfinder.global.security.handler.CustomLogoutSuccessHandler.onLogoutSuccess(..))")
	public void logAdminLogoutSuccess(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		HttpServletRequest request = (HttpServletRequest) args[0];

		// 로그 실행 전 추출
		String username = (String) request.getAttribute("username");
		String ip = IpAddressUtil.getClientIp(request);

		joinPoint.proceed();

		// 로그 남기기
		Map<String, Object> logMap = new HashMap<>();
		logMap.put("event", "Logout");
		logMap.put("user", username);
		logMap.put("ipAddress", ip);

		String jsonLog = objectMapper.writeValueAsString(logMap);
		log.info(jsonLog);
	}
}
