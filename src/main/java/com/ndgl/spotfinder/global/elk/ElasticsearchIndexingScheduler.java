package com.ndgl.spotfinder.global.elk;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
	private boolean isFullIndexing = false;

	@Autowired
	@Qualifier("postCachdRedisTemplate")
	private final RedisTemplate<String, List<Long>> redisTemplate;

	@Scheduled(cron = "0 0 4 * * *") // 매일 04시에 전체 색인
	public void fullReindexPosts() {
		if (isFullIndexing) { // 이미 전체 인덱싱 중이면 종료
			return;
		}
		isFullIndexing = true;

		log.info("전체 인덱싱 수행");
		List<Post> posts = postRepository.findAllWithAssociations();

		List<PostDocument> documents = posts.stream()
			.map(PostDocument::from)
			.toList();

		postRepository.deleteAll();
		postSearchRepository.saveAll(documents);

		redisTemplate.delete("search:post:*");
		redisTemplate.delete("searchJpa:post:*");

		isFullIndexing = false;
	}

	@Transactional(readOnly = true)
	@Scheduled(cron = "0 */10 * * * *") // 10분마다 부분 색인
	public void partialReindexPosts() {
		if (isFullIndexing) {
			return; // 전체 인덱싱 중이면 부분 인덱싱 건너뜀
		}
		log.info("부분 인덱싱 수행");
		LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

		List<Post> recentPosts = postRepository.findByUpdatedAtAfter(tenMinutesAgo);

		List<PostDocument> documents = recentPosts.stream()
			.map(PostDocument::from)
			.toList();

		postSearchRepository.saveAll(documents);

		redisTemplate.delete("search:post:*");
		redisTemplate.delete("searchJpa:post:*");
	}
}
