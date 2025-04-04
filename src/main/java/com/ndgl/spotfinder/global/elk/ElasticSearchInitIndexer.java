package com.ndgl.spotfinder.global.elk;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.search.document.PostDocument;
import com.ndgl.spotfinder.domain.search.repository.PostSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(PostSearchRepository.class)
public class ElasticSearchInitIndexer {

	private final PostSearchRepository postSearchRepository;
	private final PostRepository postRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void initIndexing() {
		log.info("애플리케이션 기동 후 Elasticsearch 인덱싱 점검 시작");

		long count = postSearchRepository.count();
		if (count > 0) {
			log.info("Elasticsearch에 이미 인덱싱된 문서 수: {} → 초기화 후 재인덱싱 진행", count);
			postSearchRepository.deleteAll();
		} else {
			log.info("Elasticsearch 인덱스가 비어있음 → 인덱싱 수행");
		}

		List<Post> posts = postRepository.findAll();
		List<PostDocument> documents = posts.stream()
			.map(PostDocument::from)
			.toList();

		postSearchRepository.saveAll(documents);

		log.info("Elasticsearch 인덱싱 완료: {}건", documents.size());
	}
}

