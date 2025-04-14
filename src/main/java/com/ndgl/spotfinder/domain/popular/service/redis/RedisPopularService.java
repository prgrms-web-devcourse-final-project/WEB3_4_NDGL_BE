package com.ndgl.spotfinder.domain.popular.service.redis;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.domain.popular.constants.PopularConstants;
import com.ndgl.spotfinder.domain.popular.dto.KeywordCountDto;
import com.ndgl.spotfinder.domain.popular.dto.PostCountDto;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPopularService {

	private final PostRepository postRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;


	private static final String POPULAR_KEYWORDS_KEY = "popular:keywords";
	private static final String POPULAR_POSTS_KEY = "popular:posts";

	// Top N 키워드 순서대로 저장
	public void updateRedisPopularKeywords(List<KeywordCountDto> keywordCounts) {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

		// 기존 ZSET 데이터 삭제
		if(!keywordCounts.isEmpty()) {
			redisTemplate.delete(POPULAR_KEYWORDS_KEY);
		}

		// 새로운 데이터 추가
		for (KeywordCountDto keywordCount : keywordCounts) {
			zSetOps.add(POPULAR_KEYWORDS_KEY, keywordCount.keyword(), keywordCount.count());
		}

		log.info("Redis 인기 검색어 업데이트 완료: {} 개 키워드", keywordCounts.size());
	}

	// Top N 포스트 순서대로 저장
	@Transactional(readOnly = true)
	public void updateRedisPopularPosts(List<PostCountDto> postCounts) {
		try {
			ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

			// 기존 ZSET 데이터 삭제
			if(!postCounts.isEmpty()) {
				redisTemplate.delete(POPULAR_POSTS_KEY);
			}

			// 새로운 데이터 추가
			for (PostCountDto postCount : postCounts) {
				Post post = postRepository.findById(postCount.postId())
					.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);
				PostResponseDto postResponseDto = new PostResponseDto(post);
				zSetOps.add(POPULAR_POSTS_KEY, objectMapper.writeValueAsString(postResponseDto), postCount.count());
			}

			log.info("Redis 인기 게시물 업데이트 완료: {} 개 게시물", postCounts.size());
		} catch (JsonProcessingException e){
			ErrorCode.JSON_PROCESSING_EXCEPTION.throwServiceException(e);
		}
	}

	// Top N 인기 검색어 조회
	public List<String> getPopularKeywords() {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

		// ZSET에서 상위 N개 항목 조회 (내림차순, 0부터 N-1까지)
		Set<ZSetOperations.TypedTuple<String>> keywordSet = zSetOps.reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, PopularConstants.KEYWORD_COUNT-1);

		if (keywordSet == null || keywordSet.isEmpty()) {
			ErrorCode.POPULAR_KEYWORD_NOT_FOUND.throwServiceException();
		}

		// KeywordCount 리스트로 변환
		List<String> popularKeywords = keywordSet.stream()
			.map(this::toKeyword)
			.toList();

		log.info("Redis에서 인기 검색어 Top 10 조회 완료: {} 개", popularKeywords.size());

		return popularKeywords;
	}

	// Top N 인기 게시물 조회
	public List<PostResponseDto> getPopularPosts() {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

		// ZSET에서 상위 N개 항목 조회 (내림차순, 0부터 N-1까지)
		Set<ZSetOperations.TypedTuple<String>> postSet = zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, PopularConstants.POST_COUNT-1);

		if (postSet == null || postSet.isEmpty()) {
			ErrorCode.POPULAR_POST_NOT_FOUND.throwServiceException();
		}

		// PostId 리스트로 변환
		List<PostResponseDto> popularPosts = postSet.stream()
			.map(this::toPostResponseDto)
			.toList();

		log.info("Redis에서 인기 게시물 Top 10 조회 완료: {} 개", popularPosts.size());

		return popularPosts;
	}

	private String toKeyword(ZSetOperations.TypedTuple<String> tuple) {
		if(isInvalidTuple(tuple))
			ErrorCode.REDIS_INVALID_ZSET_TUPLE.throwServiceException();

		return tuple.getValue();
	}

	private PostResponseDto toPostResponseDto(ZSetOperations.TypedTuple<String> tuple) {
		try {
			if (isInvalidTuple(tuple))
				ErrorCode.REDIS_INVALID_ZSET_TUPLE.throwServiceException();

			return objectMapper.readValue(tuple.getValue(), PostResponseDto.class);
		} catch(JsonProcessingException e) {
			throw ErrorCode.JSON_PROCESSING_EXCEPTION.throwServiceException(e);
		}
	}

	private boolean isInvalidTuple(ZSetOperations.TypedTuple<String> tuple) {
		return tuple == null || tuple.getValue() == null || tuple.getScore() == null;
	}
}
