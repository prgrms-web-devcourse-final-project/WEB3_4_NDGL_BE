package com.ndgl.spotfinder.domain.search.service;

import java.util.Comparator;
import java.util.List;

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

		Long lastId = request.lastId();
		int size = request.size();
		Pageable pageable = PageRequest.of(0, size + 1);
		Page<PostDocument> page = postSearchRepository.findByTitleOrContent(keyword, keyword, pageable);

		List<PostDocument> filtered = page.getContent().stream()
			.filter(post -> post.getId() > lastId)
			.sorted(Comparator.comparing(PostDocument::getId))
			.limit(size + 1)
			.toList();

		boolean hasNext = filtered.size() > size;
		List<PostDocument> content = hasNext ? filtered.subList(0, size) : filtered;

		log.info("엘라스틱서치 결과 수: {}", page.getTotalElements());
		content.forEach(post -> log.info("결과: id={}, title={}", post.getId(), post.getTitle()));

		log.info("엘라스틱서치 사용");
		return new SliceResponse<>(
			content.stream()
				.map(PostResponseDto::new)
				.toList(),
			hasNext
		);
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
}
