package com.ndgl.spotfinder.domain.post.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.KeyScanOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.post.dto.PostViewCountDto;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostViewCountService {
	private final RedisConnectionFactory redisConnectionFactory;
	private final RedisTemplate<String, String> redisTemplate;
	private final KeyScanOptions postViewCountKeyOptions;
	private final PostService postService;

	private static final String POST_KEY_PREFIX = "viewed:post:";

	public List<PostViewCountDto> collectPostViewCounts() {
		List<PostViewCountDto> viewCountInfo = new ArrayList<>();
		RedisConnection connection = redisConnectionFactory.getConnection();
		Cursor<byte[]> cursor = connection.scan(postViewCountKeyOptions);

		while (cursor.hasNext()) {
			String key = new String(cursor.next());
			Long postId = extractPostIdFromKey(key);
			Long viewCount = redisTemplate.opsForSet().size(key);

			viewCountInfo.add(new PostViewCountDto(postId, viewCount));
		}

		return viewCountInfo;
	}

	@Transactional
	public void applyToDatabase(List<PostViewCountDto> viewCounts) {
		viewCounts.forEach(postViewCount ->
			postService.incrementPostViewCount(postViewCount.getPostId(), postViewCount.getViewCount())
		);

		deleteFromRedis(viewCounts);
	}

	@Transactional
	public void deleteFromRedis(List<PostViewCountDto> viewCounts) {
		List<String> keys = viewCounts
			.stream()
			.map(postViewCount -> mapPostIdToRedisKey(postViewCount.getPostId()))
			.toList();

		redisTemplate.delete(keys);
	}

	private Long extractPostIdFromKey(String key) {
		try {
			String[] parts = key.split(POST_KEY_PREFIX);

			return Long.parseLong(parts[1]);
		} catch (Exception e) {
			throw ErrorCode.VIEW_COUNT_KEY_EXTRACT_ERROR.throwServiceException(e);
		}
	}

	private String mapPostIdToRedisKey(Long postId) {
		return POST_KEY_PREFIX + postId;
	}
}
