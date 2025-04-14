package com.ndgl.spotfinder.global.elk;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.search.document.PostDocument;
import com.ndgl.spotfinder.domain.search.repository.PostSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexingScheduler {
	private final PostRepository postRepository;
	private final PostSearchRepository postSearchRepository;

	@Scheduled(cron = "0 0 0 * * *") // 매일 자정에 전체 색인
	public void fullReindexPosts() {
		log.info("전체 인덱싱 수행");
		List<Post> posts = postRepository.findAllWithAssociations();

		List<PostDocument> documents = posts.stream()
			.map(PostDocument::from)
			.toList();

		postSearchRepository.saveAll(documents);
	}

	@Scheduled(cron = "0 */30 * * * *") // 30분마다 부분 색인
	public void partialReindexPosts() {
		log.info("부분 인덱싱 수행");
		LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

		List<Post> recentPosts = postRepository.findByUpdatedAtAfter(thirtyMinutesAgo);

		List<PostDocument> documents = recentPosts.stream()
			.map(PostDocument::from)
			.toList();

		postSearchRepository.saveAll(documents);
	}
}
