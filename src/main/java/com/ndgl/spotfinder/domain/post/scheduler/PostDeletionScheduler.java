package com.ndgl.spotfinder.domain.post.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.post.type.PostStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostDeletionScheduler {

	private final PostRepository postRepository;
	private final PostService postService;

	@Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
	public void hardDeleteScheduledPosts() {
		log.info("------- Post 삭제 스케줄러 시작 -------");
		LocalDate today = LocalDate.now();

		List<Post> postsToDelete = postRepository.findAllByStatusAndDeleteScheduledAt(
			PostStatus.DELETED, today
		);

		for (Post post : postsToDelete) {
			log.info("삭제된 PostId : {}", post.getId());
			postService.deletePost(post.getId());
		}
		log.info("------- Post 삭제 스케줄러 종료 -------");
	}
}
