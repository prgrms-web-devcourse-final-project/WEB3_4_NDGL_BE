package com.ndgl.spotfinder.domain.popular.redis;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.domain.popular.constants.PopularConstants;
import com.ndgl.spotfinder.domain.popular.dto.KeywordCountDto;
import com.ndgl.spotfinder.domain.popular.dto.PostCountDto;
import com.ndgl.spotfinder.domain.popular.service.redis.RedisPopularService;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.type.PostStatus;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class RedisPopularServiceTest {

	private static final String POPULAR_KEYWORDS_KEY = "popular:keywords";
	private static final String POPULAR_POSTS_KEY = "popular:posts";
	private final ObjectMapper realObjectMapper = new ObjectMapper();
	@Mock
	private RedisTemplate<String, String> redisTemplate;
	@Mock
	private ZSetOperations<String, String> zSetOps;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private PostRepository postRepository;
	@InjectMocks
	private RedisPopularService redisPopularService;

	@BeforeEach
	void setUp() {
		when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
	}

	@Test
	@DisplayName("레디스 인기 검색어 갱신 - 정상")
	void updateRedisPopularKeywords_success() {
		// Given
		List<KeywordCountDto> keywordCountDtos = List.of(
			new KeywordCountDto("키워드1", 50L),
			new KeywordCountDto("키워드2", 100L)
		);

		redisPopularService.updateRedisPopularKeywords(keywordCountDtos);

		verify(redisTemplate).delete(POPULAR_KEYWORDS_KEY);
		verify(zSetOps).add(POPULAR_KEYWORDS_KEY, "키워드1", 50.0);
		verify(zSetOps).add(POPULAR_KEYWORDS_KEY, "키워드2", 100.0);
	}

	@Test
	@DisplayName("레디스 인기 게시물 갱신 - 정상")
	void updateRedisPopularPosts() {
		// Given
		User user = mock(User.class);
		when(user.getId()).thenReturn(1L);
		when(user.getNickName()).thenReturn("test");

		Post post1 = Post.builder()
			.id(1L)
			.title("테스트1")
			.content("테스트 내용1")
			.user(user)
			.thumbnail("https://example.com/" + user.getId() + "_thumbnail.jpg")
			.viewCount(10L)
			.likeCount(0L)
			.status(PostStatus.PUBLIC)
			.build();

		Post post2 = Post.builder()
			.id(2L)
			.title("테스트2")
			.content("테스트 내용2")
			.user(user)
			.thumbnail("https://example.com/" + user.getId() + "_thumbnail.jpg")
			.viewCount(10L)
			.likeCount(0L)
			.status(PostStatus.PUBLIC)
			.build();

		List<PostCountDto> postCountDtos = List.of(
			new PostCountDto(post1.getId(), 50L),
			new PostCountDto(post2.getId(), 100L)
		);

		when(postRepository.findById(post1.getId())).thenReturn(Optional.of(post1));
		when(postRepository.findById(post2.getId())).thenReturn(Optional.of(post2));

		// When
		redisPopularService.updateRedisPopularPosts(postCountDtos);

		// Then
		verify(redisTemplate).delete(POPULAR_POSTS_KEY);
		verify(zSetOps).add(eq(POPULAR_POSTS_KEY), any(), eq(50.0));
		verify(zSetOps).add(eq(POPULAR_POSTS_KEY), any(), eq(100.0));
	}

	@Test
	@DisplayName("Top N 인기 검색어 조회 - 정상")
	void getPopularKeywords_success() {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		mockSet.add(mockTypedTuple("키워드1", 100.0));
		mockSet.add(mockTypedTuple("키워드2", 50.0));

		when(zSetOps.reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, 9)).thenReturn(mockSet);

		//when
		List<String> result = redisPopularService.getPopularKeywords();

		//then
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isEqualTo("키워드1");
		assertThat(result.get(1)).isEqualTo("키워드2");
	}

	@Test
	@DisplayName("Top N 인기 검색어 조회 - 결과 X")
	void getPopularKeywords_popularKeywords_not_found_fail() {
		// Given
		when(zSetOps.reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, 9)).thenReturn(Collections.emptySet());

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularKeywords())
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException)exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.POPULAR_KEYWORD_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("Top N 인기 검색어 조회 - 비정상 null Tuple")
	void getPopularKeywords_null_tuple_fail() {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		mockSet.add(null);

		when(zSetOps.reverseRangeWithScores(POPULAR_KEYWORDS_KEY, 0, 9)).thenReturn(mockSet);

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularKeywords())
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException)exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REDIS_INVALID_ZSET_TUPLE.getMessage());
			});
	}

	@Test
	@DisplayName("Top N 인기 게시물 조회 - 정상")
	void getPopularPosts_success() throws Exception {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();

		User user = mock(User.class);
		when(user.getId()).thenReturn(1L);
		when(user.getNickName()).thenReturn("test");

		Post post1 = Post.builder()
			.id(1L)
			.title("테스트1")
			.content("테스트 내용1")
			.user(user)
			.thumbnail("https://example.com/" + user.getId() + "_thumbnail.jpg")
			.viewCount(10L)
			.likeCount(0L)
			.status(PostStatus.PUBLIC)
			.build();

		Post post2 = Post.builder()
			.id(2L)
			.title("테스트2")
			.content("테스트 내용2")
			.user(user)
			.thumbnail("https://example.com/" + user.getId() + "_thumbnail.jpg")
			.viewCount(10L)
			.likeCount(0L)
			.status(PostStatus.PUBLIC)
			.build();
		PostResponseDto postDto1 = new PostResponseDto(post1);
		PostResponseDto postDto2 = new PostResponseDto(post2);

		String postJson1 = realObjectMapper.writeValueAsString(postDto1);
		String postJson2 = realObjectMapper.writeValueAsString(postDto2);

		mockSet.add(mockTypedTuple(postJson1, 100.0));
		mockSet.add(mockTypedTuple(postJson2, 50.0));

		when(zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, PopularConstants.POST_COUNT - 1)).thenReturn(mockSet);
		when(objectMapper.readValue(postJson1, PostResponseDto.class)).thenReturn(postDto1);
		when(objectMapper.readValue(postJson2, PostResponseDto.class)).thenReturn(postDto2);

		//when
		List<PostResponseDto> result = redisPopularService.getPopularPosts();

		//then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).id()).isEqualTo(1);
		assertThat(result.get(1).id()).isEqualTo(2);
	}

	@Test
	@DisplayName("Top N 인기 게시물 조회 - 결과 X")
	void getPopularPosts_popularPosts_not_found_fail() {
		// Given
		when(zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, 9)).thenReturn(Collections.emptySet());

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularPosts())
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException)exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.POPULAR_POST_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("Top N 인기 게시물 조회 - 비정상 null tuple")
	void getPopularPosts_null_tuple_fail() {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		mockSet.add(null);

		when(zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, 9)).thenReturn(mockSet);

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularPosts())
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException)exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REDIS_INVALID_ZSET_TUPLE.getMessage());
			});
	}

	@Test
	@DisplayName("Top N 인기 게시물 조회 - json 파싱 불가능한 튜플")
	void getPopularPosts_non_json_tuple_fail() throws Exception {
		Set<ZSetOperations.TypedTuple<String>> mockSet = new LinkedHashSet<>();
		String invalidJson = "invalid";
		mockSet.add(mockTypedTuple(invalidJson, 100.0));

		when(zSetOps.reverseRangeWithScores(POPULAR_POSTS_KEY, 0, 9)).thenReturn(mockSet);
		when(objectMapper.readValue(invalidJson, PostResponseDto.class))
			.thenThrow(new JsonMappingException(null, "invalid Json"));

		// When & Then
		assertThatThrownBy(() -> redisPopularService.getPopularPosts())
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException)exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.JSON_PROCESSING_EXCEPTION.getMessage());
				assertThat(serviceException.getCause()).isInstanceOf(JsonProcessingException.class);
			});
	}

	private ZSetOperations.TypedTuple<String> mockTypedTuple(String value, double score) {
		ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
		when(tuple.getValue()).thenReturn(value);
		when(tuple.getScore()).thenReturn(score);
		return tuple;
	}
}
