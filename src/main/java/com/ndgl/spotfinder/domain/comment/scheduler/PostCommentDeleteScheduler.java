package com.ndgl.spotfinder.domain.comment.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCommentDeleteScheduler {
	private final PostCommentRepository postCommentRepository;

	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void deleteSoftDeletedComments() {
		log.info("댓글 삭제 스케줄러 실행");
		List<PostComment> deletableComments = postCommentRepository.findDeletableComments();
		postCommentRepository.deleteAll(deletableComments);
	}
}
