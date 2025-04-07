package com.ndgl.spotfinder.domain.search.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

	public PostSearchService(
		PostService postService,
		PostRepository postJpaRepository,
		ElasticSearchHealthCheck healthCheck,
		@Autowired(required = false) PostSearchRepository postSearchRepository
	) {
		this.postService = postService;
		this.postRepository = postJpaRepository;
		this.healthCheck = healthCheck;
		this.postSearchRepository = postSearchRepository;
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
		if (postSearchRepository == null) {  // ES 서버 비활성 재확인 수행
			log.info("엘라스틱서치 실패");
			return searchWithJpa(request, keyword);
		}

		Long lastId = postService.getLastPostId(request);
		int size = request.size();
		Pageable pageable = PageRequest.of(0, size + 1);
		Page<PostDocument> page = postSearchRepository.searchByKeyword(keyword, pageable);

		List<Long> filteredIds = page.getContent().stream()
			.filter(post -> post.getId() < lastId)
			.sorted(Comparator.comparing(PostDocument::getId).reversed())
			.limit(size + 1)
			.map(PostDocument::getId)
			.toList();

		boolean hasNext = filteredIds.size() > size;
		List<Long> slicedIds = hasNext ? filteredIds.subList(0, size) : filteredIds;

		// 실제 Post 조회
		List<Post> posts = postRepository.findAllById(slicedIds);

		// 순서 보존용 Map
		Map<Long, Post> postMap = posts.stream()
			.collect(Collectors.toMap(Post::getId, Function.identity()));

		List<PostResponseDto> result = slicedIds.stream()
			.map(postMap::get)
			.filter(Objects::nonNull)
			.map(PostResponseDto::new)
			.toList();

		log.info("엘라스틱서치 사용 - 결과 수: {}", result.size());
		return new SliceResponse<>(result, hasNext);
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

	@Transactional(readOnly = true)
	public List<PostResponseDto> searchPostsToList(String keyword) {
		List<Long> postIds = postSearchRepository.searchAllByKeyword(keyword).stream()
			.map(PostDocument::getId)
			.toList();
		log.info("엘라스틱서치 검색 결과 수: {}", postIds.size());

		List<Post> posts = postRepository.findAllById(postIds);

		// 순서 보존을 위해 Map 변환
		Map<Long, Post> postMap = posts.stream()
			.collect(Collectors.toMap(Post::getId, Function.identity()));

		// postIds 순서대로 Post → DTO 변환
		return postIds.stream()
			.map(postMap::get)
			.filter(Objects::nonNull)
			.map(PostResponseDto::new)
			.toList();
	}
}
