package com.ndgl.spotfinder.domain.search;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.search.document.PostDocument;
import com.ndgl.spotfinder.domain.search.repository.PostSearchRepository;
import com.ndgl.spotfinder.domain.search.service.PostSearchService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.elk.ElasticSearchHealthCheck;

@ActiveProfiles("test")
@SpringBootTest
public class PostSearchServiceTest {
	@InjectMocks
	private PostSearchService postSearchService;

	@Mock
	private PostService postService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private PostSearchRepository postSearchRepository;

	@Mock
	private ElasticSearchHealthCheck healthCheck;

	private final User user1 = User.builder()
		.id(1L)
		.email("이메일1")
		.nickName("별명1")
		.blogName("블로그1")
		.build();

	private final Post samplePost = Post.builder()
		.id(1L)
		.title("제목1")
		.content("내용1")
		.user(user1)
		.build();

	private final PostDocument doc1 = PostDocument.builder()
		.id(2L)
		.title("맛집 추천")
		.content("정말 맛있어요")
		.build();

	private final PostDocument doc2 = PostDocument.builder()
		.id(3L)
		.title("여행 후기")
		.content("풍경이 좋아요")
		.build();
	@Autowired
	private RedisTemplate<String, List<Long>> redisTemplate;

	@Test
	@DisplayName("JPA 기반 like 검색")
	void search_with_jpa() {
		// given
		String keyword = "맛";
		Long lastId = 10L;
		int size = 3;

		PageRequest pageRequest = PageRequest.of(0, size + 1);
		List<Post> posts = List.of(samplePost);
		Slice<Post> postSlice = new SliceImpl<>(posts, pageRequest, false);

		when(healthCheck.isElasticSearchUp()).thenReturn(false);
		when(postService.getLastPostId(any(SliceRequest.class))).thenReturn(lastId);
		when(postRepository.searchAll(eq(keyword), any(PageRequest.class)))
			.thenReturn(postSlice);

		postSearchService = new PostSearchService(
			postService, postRepository, healthCheck, null, redisTemplate // ES 비활성화
		);

		// when
		SliceResponse<PostResponseDto> result = postSearchService.searchPosts(
			new SliceRequest(lastId, size),
			keyword
		);

		// then
		assertEquals(1, result.contents().size());
		assertEquals("제목1", result.contents().get(0).title());
		assertEquals("별명1", result.contents().get(0).authorName());
		assertFalse(result.hasNext());

		verify(postRepository, times(1)).searchAll(eq(keyword), any(PageRequest.class));
	}

	@Test
	@DisplayName("Elasticsearch 기반 검색 수행")
	void search_with_elasticsearch() {
		// given
		String keyword = "여행";
		Long lastId = Long.MAX_VALUE;
		int size = 2;

		List<PostDocument> docs = List.of(doc1, doc2); // 2개의 문서 (id: 2, 3)
		Page<PostDocument> esPage = new PageImpl<>(docs);

		when(healthCheck.isElasticSearchUp()).thenReturn(true);
		when(postSearchRepository.searchByKeyword(eq(keyword), any(PageRequest.class)))
			.thenReturn(esPage);
		when(postService.getLastPostId(any(SliceRequest.class))).thenReturn(lastId);
		when(postRepository.findAllById(anyList())).thenReturn(
			List.of(Post.builder().id(2L).title("맛집 추천").build(),
				Post.builder().id(3L).title("여행 후기").build())
		);

		postSearchService = new PostSearchService(
			postService, postRepository, healthCheck, postSearchRepository, redisTemplate
		);

		// when
		SliceResponse<PostResponseDto> result = postSearchService.searchPosts(
			new SliceRequest(lastId, size),
			keyword
		);

		// then
		assertEquals(2, result.contents().size());
		assertEquals("맛집 추천", result.contents().get(0).title());
		assertEquals("여행 후기", result.contents().get(1).title());
		assertFalse(result.hasNext());
	}

	@Test
	@DisplayName("Elasticsearch 사용 중 예외로 JPA 검색으로 fallback")
	void search_elasticsearch_fallback_to_jpa() {
		// given
		String keyword = "맛";
		Long lastId = Long.MAX_VALUE;
		int size = 3;

		when(healthCheck.isElasticSearchUp()).thenReturn(true);
		when(postSearchRepository.searchByKeyword(eq(keyword), any(PageRequest.class)))
			.thenThrow(new RuntimeException("Elasticsearch down"));
		when(postService.getLastPostId(any(SliceRequest.class))).thenReturn(lastId);
		when(postRepository.searchAll(eq(keyword), any(PageRequest.class)))
			.thenReturn(new SliceImpl<>(List.of(samplePost), PageRequest.of(0, size + 1), false));

		postSearchService = new PostSearchService(
			postService, postRepository, healthCheck, postSearchRepository, redisTemplate
		);

		// when
		SliceResponse<PostResponseDto> result = postSearchService.searchPosts(
			new SliceRequest(lastId, size),
			keyword
		);

		// then
		assertEquals(1, result.contents().size());
		assertEquals("제목1", result.contents().get(0).title());
		assertFalse(result.hasNext());

		verify(postRepository, times(1)).searchAll(eq(keyword), any(PageRequest.class));
	}
}
