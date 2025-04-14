package com.ndgl.spotfinder.domain.search.service;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.popular.service.redis.RedisPopularService;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.search.document.PostDocument;
import com.ndgl.spotfinder.domain.search.document.SearchType;
import com.ndgl.spotfinder.domain.search.repository.PostSearchRepository;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.elk.ElasticsearchHealthCheck;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostSearchService {
	private final PostService postService;
	private final PostRepository postRepository;
	private final ElasticsearchHealthCheck healthCheck;
	private final PostSearchRepository postSearchRepository;
	private final RedisPopularService redisPopularService;

	@Qualifier("postCachdRedisTemplate")
	private final RedisTemplate<String, List<Long>> redisTemplate;

	public PostSearchService(
		PostService postService,
		PostRepository postJpaRepository,
		ElasticsearchHealthCheck healthCheck,
		@Autowired(required = false) PostSearchRepository postSearchRepository,
		RedisTemplate<String, List<Long>> redisTemplate,
		RedisPopularService redisPopularService
	) {
		this.postService = postService;
		this.postRepository = postJpaRepository;
		this.healthCheck = healthCheck;
		this.postSearchRepository = postSearchRepository;
		this.redisTemplate = redisTemplate;
		this.redisPopularService = redisPopularService;
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> searchPosts(SliceRequest request, String keyword) {
		boolean esAvailable = healthCheck.isElasticSearchUp() && postSearchRepository != null;

		if (esAvailable) {
			return searchWith(SearchType.ELASTICSEARCH, request, keyword);
		} else {
			log.info("엘라스틱서치 서버 비활성화 - JPA 검색으로 대체");
			return searchWith(SearchType.JPA, request, keyword);
		}
	}

	private SliceResponse<PostResponseDto> searchWith(SearchType type, SliceRequest request, String keyword) {
		String prefix = type == SearchType.ELASTICSEARCH ? "search:post:" : "searchJpa:post:";
		String redisKey = prefix + keyword;

		List<Long> cachedIds = getCachedIds(redisKey);

		if (cachedIds == null || cachedIds.isEmpty()) {
			log.info("검색어 [{}]에 대한 캐시 없음 - {} 검색 수행", keyword, type);
			cachedIds = fetchAndCacheSearchResults(keyword, redisKey, type);
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

	private List<Long> fetchAndCacheSearchResults(String keyword, String redisKey, SearchType type) {
		List<Long> ids;

		if (type == SearchType.ELASTICSEARCH) {
			Page<PostDocument> page = postSearchRepository.searchByKeyword(keyword, PageRequest.of(0, 1000));
			ids = page.getContent().stream()
				.map(PostDocument::getId)
				.toList();

			log.info("Elasticsearch 검색 결과 [{}]건 캐싱", ids.size());

		} else { // JPA
			PageRequest pageRequest = PageRequest.of(0, 1000);
			Slice<Post> posts = postRepository.searchAll(keyword, pageRequest);

			ids = posts.getContent().stream()
				.sorted(Comparator.comparing(Post::getId).reversed())
				.map(Post::getId)
				.toList();

			log.info("JPA 검색 결과 [{}]건 캐싱", ids.size());
		}

		redisTemplate.opsForValue().set(redisKey, ids, Duration.ofMinutes(10));
		return ids;
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

	@Transactional(readOnly = true)
	public List<String> suggestKeyword(String keyword) {
		return Optional.ofNullable(postSearchRepository)
			.map(repo -> repo.suggestKeyword(keyword))
			.orElse(Collections.emptyList());
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

	public List<String> getPopularKeywords() {
		return redisPopularService.getPopularKeywords();
	}
}
