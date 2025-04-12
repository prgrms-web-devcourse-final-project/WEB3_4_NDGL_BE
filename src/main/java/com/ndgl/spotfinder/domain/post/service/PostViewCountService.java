package com.ndgl.spotfinder.domain.post.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.KeyScanOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.post.entity.Post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostViewCountService {
	private final RedisTemplate<String, String> redisTemplate;
	private final PostService postService;

	private static final String POST_KEY_PREFIX = "viewed:post:";
	private static final long SCAN_BATCH_SIZE = 1000;

	public Map<Long, Long> collectPostViewCounts() {
		Map<Long, Long> viewCountMap = new HashMap<>();
		RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();

		KeyScanOptions options = (KeyScanOptions)ScanOptions
			.scanOptions()
			.type(DataType.SET)
			.match(POST_KEY_PREFIX + "*")
			.count(SCAN_BATCH_SIZE)
			.build();

		try (Cursor<byte[]> cursor = connection.scan(options)) {
			while (cursor.hasNext()) {
				String key = new String(cursor.next());
				Long postId = extractPostIdFromKey(key);
				Long viewCount = redisTemplate.opsForSet().size(key);

				viewCountMap.put(postId, viewCount);
			}
		} catch (Exception e) {
			log.error("조회수를 가져오지 못했습니다.");
		}

		return viewCountMap;
	}

	@Transactional
	public void applyToDatabase(Map<Long, Long> viewCounts) {
		List<Long> postIds = viewCounts.keySet().stream().toList();
		List<Post> posts = postService.findPostByIds(postIds);

		posts.forEach(post -> {
			post.addViewCount(viewCounts.get(post.getId()));
		});

		deleteFromRedis(viewCounts);
	}

	@Transactional
	public void deleteFromRedis(Map<Long, Long> viewCounts) {
		List<String> keys = viewCounts.keySet()
			.stream()
			.map(this::mapPostIdToRedisKey)
			.toList();

		redisTemplate.delete(keys);
	}

	private Long extractPostIdFromKey(String key) {
		try {
			String[] parts = key.split(POST_KEY_PREFIX);

			return Long.parseLong(parts[1]);
		} catch (Exception e) {
			log.error("잘못된 키 값입니다.");
			throw e;
		}
	}

	private String mapPostIdToRedisKey(Long postId) {
		return POST_KEY_PREFIX + postId;
	}
}
