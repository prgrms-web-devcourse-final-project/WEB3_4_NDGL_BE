package com.ndgl.spotfinder.domain.search;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.popular.service.redis.RedisPopularService;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.post.type.PostStatus;
import com.ndgl.spotfinder.domain.search.document.PostDocument;
import com.ndgl.spotfinder.domain.search.repository.PostSearchRepository;
import com.ndgl.spotfinder.domain.search.service.PostSearchService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.elk.ElasticsearchHealthCheck;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class PostSearchServiceTest {
	@Mock
	private RedisTemplate<String, List<Long>> redisTemplate;

	@Mock
	private ValueOperations<String, List<Long>> valueOperations;

	@InjectMocks
	private PostSearchService postSearchService;

	@Mock
	private PostService postService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private PostSearchRepository postSearchRepository;

	@Mock
	private ElasticsearchHealthCheck healthCheck;

	@Mock
	private RedisPopularService redisPopularService;

	private final User user1 = User.builder()
		.id(1L)
		.email("이메일1")
		.nickName("별명1")
		.blogName("블로그1")
		.build();

	private final Post samplePost2 = Post.builder()
		.id(2L)
		.title("맛집 추천")
		.content("정말 맛있어요")
		.user(user1)
		.status(PostStatus.PUBLIC)
		.build();

	private final Post samplePost3 = Post.builder()
		.id(3L)
		.title("여행 후기")
		.content("풍경이 좋아요")
		.user(user1)
		.status(PostStatus.BLIND)
		.build();

	@BeforeEach
	void setUp() {
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		lenient().when(valueOperations.get(anyString())).thenReturn(null);
		postSearchService = new PostSearchService(
			postService,
			postRepository,
			healthCheck,
			postSearchRepository,
			redisTemplate,
			redisPopularService
		);
	}

	@Test
	@DisplayName("JPA 기반 like 검색 - 공개 상태만 필터링")
	void search_with_jpa() {
		// given
		String keyword = "맛집";
		Long lastId = Long.MAX_VALUE;
		int size = 3;

		PageRequest pageRequest = PageRequest.of(0, 1000);
		List<Post> posts = List.of(samplePost2, samplePost3);
		Slice<Post> postSlice = new SliceImpl<>(posts, pageRequest, false);

		when(healthCheck.isElasticSearchUp()).thenReturn(false);
		when(postService.getLastPostId(any(SliceRequest.class))).thenReturn(lastId);
		when(postRepository.searchAll(eq(keyword), any(PageRequest.class))).thenReturn(postSlice);
		when(postRepository.findAllById(anyList())).thenReturn(posts);

		// when
		SliceResponse<PostResponseDto> result = postSearchService.searchPosts(
			new SliceRequest(lastId, size),
			keyword
		);

		// then
		assertEquals(1, result.contents().size());
		assertEquals("맛집 추천", result.contents().get(0).title());
		assertEquals("별명1", result.contents().get(0).authorName());
		assertFalse(result.hasNext());

		verify(postRepository, times(1)).searchAll(eq(keyword), any(PageRequest.class));
	}

	@Test
	@DisplayName("Elasticsearch 기반 검색")
	void search_with_elasticsearch() {
		// given
		String keyword = "여행";
		Long lastId = 10L;
		int size = 3;

		PageRequest pageRequest = PageRequest.of(0, size + 1);
		PostDocument document = PostDocument.from(samplePost3);
		Page<PostDocument> documentPage = new PageImpl<>(List.of(document), pageRequest, 1);

		when(healthCheck.isElasticSearchUp()).thenReturn(true);
		when(postSearchRepository.searchByKeyword(eq(keyword), any(PageRequest.class)))
			.thenReturn(documentPage);
		when(postService.getLastPostId(any(SliceRequest.class))).thenReturn(lastId);
		when(postRepository.findAllById(anyList())).thenReturn(List.of(samplePost3));

		// when
		SliceResponse<PostResponseDto> result = postSearchService.searchPosts(
			new SliceRequest(lastId, size),
			keyword
		);

		// then
		assertEquals(1, result.contents().size());
		assertEquals("여행 후기", result.contents().get(0).title());
		assertEquals("별명1", result.contents().get(0).authorName());
		assertFalse(result.hasNext());

		verify(postSearchRepository, times(1)).searchByKeyword(eq(keyword), any(PageRequest.class));
		verify(postRepository, times(1)).findAllById(anyList());
	}

	@Test
	@DisplayName("검색어 자동완성")
	void suggestKeyword() {
		// given
		String keyword = "맛";
		List<String> suggestions = List.of("맛집", "맛있는", "맛있어요");
		when(postSearchRepository.suggestKeyword(keyword)).thenReturn(suggestions);

		// when
		List<String> result = postSearchService.suggestKeyword(keyword);

		// then
		assertNotNull(result);
		assertEquals(3, result.size());
		assertTrue(result.contains("맛집"));
		assertTrue(result.contains("맛있는"));
		assertTrue(result.contains("맛있어요"));

		// verify
		verify(postSearchRepository, times(1)).suggestKeyword(keyword);
	}

	@Test
	@DisplayName("자동완성 결과가 null -> 빈 리스트 반환")
	void suggestKeyword_ifNull() {
		// given
		String keyword = "맛";
		when(postSearchRepository.suggestKeyword(keyword)).thenReturn(null);

		// when
		List<String> result = postSearchService.suggestKeyword(keyword);

		// then
		assertNotNull(result);
		assertTrue(result.isEmpty());

		// verify
		verify(postSearchRepository, times(1)).suggestKeyword(keyword);
	}
}
