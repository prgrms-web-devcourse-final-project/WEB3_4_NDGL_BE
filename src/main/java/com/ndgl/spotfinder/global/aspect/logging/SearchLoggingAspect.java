package com.ndgl.spotfinder.global.aspect.logging;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.common.util.IpAddressUtil;
import com.ndgl.spotfinder.global.common.util.RequestUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SearchLoggingAspect {

	private final ObjectMapper objectMapper;

	@Around("execution(* com.ndgl.spotfinder.domain.search.controller.PostSearchController.searchPosts(..))")
	public Object logKeyWordSearch(ProceedingJoinPoint joinPoint) throws Throwable {

		String keyword = null;

		Object[] args = joinPoint.getArgs();
		for (Object arg : args) {
			if (arg instanceof String keywordArg) {
				keyword = keywordArg;
			}
		}

		Object result = joinPoint.proceed();

		HttpServletRequest request = RequestUtil.getCurrentRequest();
		if (request == null) return result;

		// 로그 데이터 구성
		Map<String, Object> logMap = new HashMap<>();
		logMap.put("keyword", keyword);
		logMap.put("ipAddress", IpAddressUtil.getClientIp(request));
		logMap.put("userAgent", request.getHeader("User-Agent"));

		// 로그 남기기
		log.info(objectMapper.writeValueAsString(logMap));

		return result;
	}
}
