package com.ndgl.spotfinder.global.aspect.logging;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.domain.user.dto.UserLoginResponseDto;
import com.ndgl.spotfinder.global.common.util.IpAddressUtil;
import com.ndgl.spotfinder.global.common.util.MaskingUtil;
import com.ndgl.spotfinder.global.common.util.RequestUtil;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class UserAuthLoggingAspect {

	private final ObjectMapper objectMapper;

	@Around("execution(* com.ndgl.spotfinder.domain.user.controller.UserController.processGoogleLogin(..))")
	public Object logGoogleLogin(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = joinPoint.proceed();

		HttpServletRequest request = RequestUtil.getCurrentRequest();
		if (request == null) return result;

		if (result instanceof RsData<?> rsData && rsData.getCode() == 200) {
			UserLoginResponseDto loginResponse = (UserLoginResponseDto)rsData.getData();

			Map<String, Object> logMap = new HashMap<>();
			logMap.put("event", "Login");
			logMap.put("oauth", loginResponse.getProvider());
			logMap.put("user", MaskingUtil.maskEmail(loginResponse.getEmail()));
			logMap.put("userId", loginResponse.getUserId());
			logMap.put("ipAddress", IpAddressUtil.getClientIp(request));

			log.info(objectMapper.writeValueAsString(logMap));
		}
		return result;
	}

	// TODO: 로그아웃 로깅 로직 구현 필요
}

