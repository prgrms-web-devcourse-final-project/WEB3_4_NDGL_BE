package com.ndgl.spotfinder.domain.popular.redis;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;

import com.ndgl.spotfinder.domain.popular.dto.KeywordCountDto;
import com.ndgl.spotfinder.domain.popular.dto.PostCountDto;
import com.ndgl.spotfinder.domain.popular.service.redis.RedisPopularService;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class RedisPopularServiceTest {

	@Mock
	private RedisTemplate redisTemplate;

	@Mock
	private ZSetOperations<String, String> zSetOps;

	@InjectMocks
	private RedisPopularService redisPopularService;

	private static final String POPULAR_KEYWORDS_KEY = "popular:keywords";
	private static final String POPULAR_POSTS_KEY = "popular:posts";

	@BeforeEach
	void setUp() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
	}

	@Test
	@DisplayName("레디스 인기 검색어 갱신 - 정상")
	void 정상_레디스_인기_검색어_갱신(){
		// Given
		List<KeywordCountDto> keywordCountDtos = List.of(
			new KeywordCountDto("키워드1", 50L),
			new KeywordCountDto("키워드2", 100L)
		);

		redisPopularService.updateRedisPopularKeywords(keywordCountDtos);

		verify(redisTemplate).delete(POPULAR_KEYWORDS_KEY);
		verify(zSetOps).add(eq(POPULAR_KEYWORDS_KEY), eq("키워드1"), eq(50.0));
		verify(zSetOps).add(eq(POPULAR_KEYWORDS_KEY), eq("키워드2"), eq(100.0));
	}

	@Test
	@DisplayName("레디스 인기 게시물 갱신 - 정상")
	void 정상_레디스_인기_게시물_갱신(){
		// Given
		List<PostCountDto> postCountDtos = List.of(
			new PostCountDto(1L, 50L),
			new PostCountDto(2L, 100L)
		);

		redisPopularService.updateRedisPopularPosts(postCountDtos);

		verify(redisTemplate).delete(POPULAR_POSTS_KEY);
		verify(zSetOps).add(eq(POPULAR_POSTS_KEY), eq("1"), eq(50.0));
		verify(zSetOps).add(eq(POPULAR_POSTS_KEY), eq("2"), eq(100.0));
	}

	@Test
	@DisplayName("Top N 인기 검색어 조회 - 정상")
	void 정상_Top_N개_인기_검색어_조회() {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		mockSet.add(mockTypedTuple("키워드1", 100.0));
		mockSet.add(mockTypedTuple("키워드2", 50.0));

		when(zSetOps.reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, 9)).thenReturn(mockSet);

		//when
		List<KeywordCountDto> result = redisPopularService.getPopularKeywords(10);

		//then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).keyword()).isEqualTo("키워드1");
		assertThat(result.get(0).count()).isEqualTo(100L);
		assertThat(result.get(1).keyword()).isEqualTo("키워드2");
		assertThat(result.get(1).count()).isEqualTo(50L);
	}

	@Test
	@DisplayName("Top N 인기 검색어 조회 - 결과 X")
	void 비정상_Top_N개_인기_검색어_조회_빈_결과() {
		// Given
		when(zSetOps.reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, 9)).thenReturn(Collections.emptySet());

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularKeywords(10))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.POPULAR_KEYWORD_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("Top N 인기 검색어 조회 - 비정상 Tuple")
	void 비정상_Top_N개_인기_검색어_조회_비정상_Tuple() {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		mockSet.add(null);

		when(zSetOps.reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, 9)).thenReturn(mockSet);

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularKeywords(10))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REDIS_INVALID_ZSET_TUPLE.getMessage());
			});
	}

	@Test
	@DisplayName("Top N 인기 게시물 조회 - 정상")
	void 정상_Top_N개_인기_게시물_조회() {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		mockSet.add(mockTypedTuple("1", 100.0));
		mockSet.add(mockTypedTuple("2", 50.0));

		when(zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, 9)).thenReturn(mockSet);

		//when
		List<PostCountDto> result = redisPopularService.getPopularPosts(10);

		//then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).postId()).isEqualTo(1);
		assertThat(result.get(0).count()).isEqualTo(100L);
		assertThat(result.get(1).postId()).isEqualTo(2);
		assertThat(result.get(1).count()).isEqualTo(50L);
	}

	@Test
	@DisplayName("Top N 인기 게시물 조회 - 결과 X")
	void 비정상_Top_N개_인기_게시물_조회_빈_결과() {
		// Given
		when(zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, 9)).thenReturn(Collections.emptySet());

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularPosts(10))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.POPULAR_POST_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("Top N 인기 게시물 조회 - 비정상 Tuple")
	void 비정상_Top_N개_인기_게시물_조회_비정상_튜플() {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		mockSet.add(null);

		when(zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, 9)).thenReturn(mockSet);

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularPosts(10))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REDIS_INVALID_ZSET_TUPLE.getMessage());
			});
	}

	@Test
	@DisplayName("Top N 인기 게시물 조회 - key 가 숫자가 아닌 Tuple")
	void 비정상_Top_N개_인기_게시물_조회_숫자가_아닌_value_를_가진_튜플() {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		mockSet.add(mockTypedTuple("invalid", 100.0));

		when(zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, 9)).thenReturn(mockSet);

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularPosts(10))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REDIS_INVALID_ZSET_TUPLE.getMessage());
				assertThat(serviceException.getCause()).isInstanceOf(NumberFormatException.class);
			});
	}

	private ZSetOperations.TypedTuple<String> mockTypedTuple(String value, double score) {
		ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
		when(tuple.getValue()).thenReturn(value);
		when(tuple.getScore()).thenReturn(score);
		return tuple;
	}
}
