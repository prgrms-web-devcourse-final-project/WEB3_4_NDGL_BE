package com.ndgl.spotfinder.domain.search.service;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.search.document.PostDocument;
import com.ndgl.spotfinder.domain.search.repository.PostSearchRepository;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.elk.ElasticSearchHealthCheck;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostSearchService {
	private final PostService postService;
	private final PostRepository postRepository;
	private final ElasticSearchHealthCheck healthCheck;
	private final PostSearchRepository postSearchRepository;
	private final RedisTemplate<String, List<Long>> redisTemplate;

	public PostSearchService(
		PostService postService,
		PostRepository postJpaRepository,
		ElasticSearchHealthCheck healthCheck,
		@Autowired(required = false) PostSearchRepository postSearchRepository,
		RedisTemplate<String, List<Long>> redisTemplate
	) {
		this.postService = postService;
		this.postRepository = postJpaRepository;
		this.healthCheck = healthCheck;
		this.postSearchRepository = postSearchRepository;
		this.redisTemplate = redisTemplate;
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> searchPosts(SliceRequest request, String keyword) {
		if (healthCheck.isElasticSearchUp()) {
			return searchWithElasticsearch(request, keyword);
		} else {
			return searchWithJpa(request, keyword);
		}
	}

	private SliceResponse<PostResponseDto> searchWithElasticsearch(SliceRequest request, String keyword) {
		if (postSearchRepository == null) {
			log.info("엘라스틱서치 서버 비활성화 - JPA 검색으로 대체");
			return searchWithJpa(request, keyword);
		}

		String redisKey = "search:post:" + keyword;
		List<Long> cachedIds = getCachedIds(redisKey);

		if (cachedIds == null || cachedIds.isEmpty()) {
			log.info("검색어 [{}]에 대한 캐시 없음 - Elasticsearch 검색 수행", keyword);
			cachedIds = fetchAndCacheSearchResults(keyword, redisKey);
		}

		return sliceCachedResults(cachedIds, request);
	}

	private List<Long> getCachedIds(String redisKey) {
		List<?> cached = redisTemplate.opsForValue().get(redisKey);
		if (cached == null) {
			return Collections.emptyList();
		}

		return cached.stream()
			.map(id -> ((Number) id).longValue())
			.toList();
	}

	private SliceResponse<PostResponseDto> sliceCachedResults(List<Long> cachedIds, SliceRequest request) {
		Long lastId = postService.getLastPostId(request);
		int size = request.size();

		// 인덱스 기반 페이징 처리
		int startIdx = 0;
		if (lastId != Long.MAX_VALUE) {
			startIdx = cachedIds.indexOf(lastId) + 1; // lastId 다음 인덱스부터 시작
		}
		int endIdx = Math.min(startIdx + size + 1, cachedIds.size());
		List<Long> subList = cachedIds.subList(startIdx, endIdx);

		boolean hasNext = subList.size() > size;
		List<Long> slicedIds = hasNext ? subList.subList(0, size) : subList;

		List<Post> posts = postRepository.findAllById(slicedIds);
		Map<Long, Post> postMap = posts.stream()
			.collect(Collectors.toMap(Post::getId, Function.identity()));

		List<PostResponseDto> results = slicedIds.stream()
			.map(postMap::get)
			.filter(Objects::nonNull)
			.map(PostResponseDto::new)
			.toList();

		log.info("Redis 캐시 검색 결과 반환 - {}건", results.size());

		return new SliceResponse<>(results, hasNext);
	}

	private List<Long> fetchAndCacheSearchResults(String keyword, String redisKey) {
		Page<PostDocument> page = postSearchRepository.searchByKeyword(keyword, PageRequest.of(0, 1000)); // 최대 1000건 캐싱

		List<Long> ids = page.getContent().stream()
			.sorted(Comparator.comparing(PostDocument::getId).reversed())
			.map(PostDocument::getId)
			.toList();

		redisTemplate.opsForValue().set(redisKey, ids, Duration.ofMinutes(30));
		log.info("검색어 [{}] 캐싱 완료 - 총 {}건", keyword, ids.size());

		return ids;
	}

	private SliceResponse<PostResponseDto> searchWithJpa(SliceRequest request, String keyword) {
		PageRequest pageRequest = PageRequest.of(0, request.size());
		Long lastId = postService.getLastPostId(request);

		Slice<Post> posts = postRepository.searchAll(keyword, lastId, pageRequest);

		log.info("JPA 사용");
		return new SliceResponse<>(
			posts.getContent().stream()
				.map(PostResponseDto::new)
				.toList(),
			posts.hasNext()
		);
	}

	@Transactional(readOnly = true)
	public void indexPosts() {
		List<Post> posts = postRepository.findAll();
		List<PostDocument> documents = posts.stream()
			.map(PostDocument::from)
			.toList();

		postSearchRepository.deleteAll();
		postSearchRepository.saveAll(documents);
	}
}
