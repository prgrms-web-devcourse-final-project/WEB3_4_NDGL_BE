package com.ndgl.spotfinder.domain.popular.service.redis;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.popular.dto.KeywordCount;
import com.ndgl.spotfinder.domain.popular.dto.PostCount;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPopularService {
	private final RedisTemplate<String, String> redisTemplate;
	private static final String POPULAR_KEYWORDS_KEY = "popular:keywords";
	private static final String POPULAR_POSTS_KEY = "popular:posts";

	// Top N 키워드 순서대로 저장
	public void updateRedisPopularKeywords(List<KeywordCount> keywords) {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

		// 기존 ZSET 데이터 삭제
		redisTemplate.delete(POPULAR_KEYWORDS_KEY);

		// 새로운 데이터 추가
		for (KeywordCount keyword : keywords) {
			zSetOps.add(POPULAR_KEYWORDS_KEY, keyword.keyword(), keyword.count());
		}

		log.info("Redis 인기 검색어 업데이트 완료: {} 개 키워드", keywords.size());
	}

	// Top N 포스트 순서대로 저장
	public void updateRedisPopularPosts(List<PostCount> posts) {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

		// 기존 ZSET 데이터 삭제
		redisTemplate.delete(POPULAR_POSTS_KEY);

		// 새로운 데이터 추가
		for (PostCount post : posts) {
			zSetOps.add(POPULAR_POSTS_KEY, String.valueOf(post.postId()), post.count());
		}

		log.info("Redis 인기 게시물 업데이트 완료: {} 개 게시물", posts.size());
	}

	// Top N 인기 검색어 조회
	public List<KeywordCount> getPopularKeywords(int size) {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

		// ZSET에서 상위 N개 항목 조회 (내림차순, 0부터 N-1까지)
		Set<ZSetOperations.TypedTuple<String>> keywordSet = zSetOps.reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, size-1);

		if (keywordSet == null || keywordSet.isEmpty()) {
			ErrorCode.POPULAR_KEYWORD_NOT_FOUND.throwServiceException();
		}

		// KeywordCount 리스트로 변환
		List<KeywordCount> result = keywordSet.stream()
			.map(this::toKeywordCount)
			.toList();

		log.info("Redis에서 인기 검색어 Top 10 조회 완료: {} 개", result.size());
		return result;
	}

	// Top N 인기 게시물 조회
	public List<PostCount> getPopularPosts(int size) {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

		// ZSET에서 상위 N개 항목 조회 (내림차순, 0부터 N-1까지)
		Set<ZSetOperations.TypedTuple<String>> postSet = zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, size-1);

		if (postSet == null || postSet.isEmpty()) {
			ErrorCode.POPULAR_POST_NOT_FOUND.throwServiceException();
		}

		// PostCount 리스트로 변환
		List<PostCount> result = postSet.stream()
			.map(this::toPostCount)
			.toList();

		log.info("Redis에서 인기 게시물 Top 10 조회 완료: {} 개", result.size());
		return result;
	}

	private boolean isInvalidTuple(ZSetOperations.TypedTuple<String> tuple) {
		return tuple == null || tuple.getValue() == null || tuple.getScore() == null;
	}

	private KeywordCount toKeywordCount(ZSetOperations.TypedTuple<String> tuple) {
		if(isInvalidTuple(tuple))
			ErrorCode.REDIS_INVALID_ZSET_TUPLE.throwServiceException();

		String keyword = tuple.getValue();
		long score = tuple.getScore().longValue();
		return new KeywordCount(keyword, score);
	}

	private PostCount toPostCount(ZSetOperations.TypedTuple<String> tuple) {
		if(isInvalidTuple(tuple))
			ErrorCode.REDIS_INVALID_ZSET_TUPLE.throwServiceException();

		try {
			long postId = Long.parseLong(tuple.getValue());
			long score = tuple.getScore().longValue();
			return new PostCount(postId, score);
		} catch(NumberFormatException e) {
			throw ErrorCode.REDIS_INVALID_ZSET_TUPLE.throwServiceException(e);
		}
	}
}
