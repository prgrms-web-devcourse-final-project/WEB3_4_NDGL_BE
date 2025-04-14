package com.ndgl.spotfinder.global.aspect.logging;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.common.util.IpAddressUtil;
import com.ndgl.spotfinder.global.common.util.RequestUtil;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SearchLoggingAspect {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String SEARCH_KEY_PREFIX = "searched:keyword:";
	private static final Duration CACHE_DURATION_TIME = Duration.ofMinutes(1);

	@Before(value = "execution(* com.ndgl.spotfinder.domain.search.service.PostSearchService.searchPosts(..)) && args(.., keyword)")
	public void logKeyWordSearch(String keyword) {

		HttpServletRequest request = RequestUtil.getCurrentRequest();
		if (request == null) return;

		String ip = IpAddressUtil.getClientIp(request);
		String userAgent = request.getHeader("User-Agent");
		String searchedKeyword = SEARCH_KEY_PREFIX + keyword + ":" + ip;

		// 최근 검색 여부 확인 및 캐싱
		if(isKeywordRecentlySearched(searchedKeyword)) {
			return;
		}

		// 검색어 로깅
		loggingSearchedKeyword(keyword, ip, userAgent);
	}

	private boolean isKeywordRecentlySearched(String searchedKeyword) {
		Boolean isExist = redisTemplate.hasKey(searchedKeyword);
		if (Boolean.TRUE.equals(isExist)) {
			return true;
		}

		redisTemplate.opsForValue().set(searchedKeyword, "true", CACHE_DURATION_TIME);
		return false;
	}

	private void loggingSearchedKeyword(String keyword, String ip, String userAgent) {
		// 로그 데이터 구성
		Map<String, Object> logMap = new HashMap<>();
		logMap.put("keyword", keyword);
		logMap.put("ipAddress", ip);
		logMap.put("userAgent", userAgent);

		// 로그 남기기
		try {
			log.info(objectMapper.writeValueAsString(logMap));
		} catch (JsonProcessingException e) {
			ErrorCode.JSON_PROCESSING_EXCEPTION.throwServiceException(e);
		}
	}
}
