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

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PostViewTrackingAspect {
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String POST_KEY_PREFIX = "viewed:post:";
	private static final Long EXPIRE_NOT_SET = -1L;
	private static final Duration CACHE_DURATION_TIME = Duration.ofMinutes(3);

	@Before(value = "execution(* com.ndgl.spotfinder.domain.post.service.PostService.getPost(..)) && args(.., postId)")
	public void handlePostViewCount(Long postId) {
		HttpServletRequest request = RequestUtil.getCurrentRequest();
		if(request == null) return;

		String ip = IpAddressUtil.getClientIp(request);
		String userAgent = request.getHeader("User-Agent");
		String viewedPost = POST_KEY_PREFIX + postId;

		Long count = redisTemplate.opsForSet().add(viewedPost, ip);
		if(count != null && count.equals(1L)) {
			loggingPostView(postId, ip, userAgent);
		}

		setExpire(viewedPost);
	}

	private void setExpire(String key) {
		Long ttl = redisTemplate.getExpire(key);

		if (ttl.equals(EXPIRE_NOT_SET)) {
			redisTemplate.expire(key, CACHE_DURATION_TIME);
		}
	}

	private void loggingPostView(Long postId, String ip, String userAgent) {
		try {
			// 로그 데이터 구성
			Map<String, Object> logMap = new HashMap<>();
			logMap.put("postId", postId);
			logMap.put("ipAddress", ip);
			logMap.put("userAgent", userAgent);

			// 로그 남기기
			log.info(objectMapper.writeValueAsString(logMap));
		} catch (JsonProcessingException e) {
			ErrorCode.JSON_PROCESSING_EXCEPTION.throwServiceException(e);
		}
	}
}
