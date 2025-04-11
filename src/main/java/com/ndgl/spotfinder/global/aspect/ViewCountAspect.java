package com.ndgl.spotfinder.global.aspect;

import java.time.Duration;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ViewCountAspect {
	private final RedisTemplate<String, String> redisTemplate;
	private final HttpServletRequest request;

	private static final String POST_KEY_PREFIX = "viewed:post:";
	private static final Long EXPIRE_NOT_SET = -1L;
	private static final Duration CACHE_DURATION_TIME = Duration.ofHours(1);

	@Before(value = "execution(* com.ndgl.spotfinder.domain.post.service.PostService.getPost(..)) && args(id)")
	public void handlePostViewCount(Long id) {
		String ip = request.getRemoteAddr();
		String viewedPost = POST_KEY_PREFIX + id;

		redisTemplate.opsForSet().add(viewedPost, ip);
		setExpire(viewedPost);
	}

	private void setExpire(String key) {
		Long ttl = redisTemplate.getExpire(key);

		if (ttl.equals(EXPIRE_NOT_SET)) {
			redisTemplate.expire(key, CACHE_DURATION_TIME);
		}
	}
}
