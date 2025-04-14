package com.ndgl.spotfinder.domain.post.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.domain.post.dto.PostViewCountDto;
import com.ndgl.spotfinder.domain.post.service.PostViewCountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewSyncScheduler {
	private final PostViewCountService postViewCountService;

	private static final long POST_VIEW_COUNT_SYNC_PERIOD = 60 * 1000;

	@Scheduled(fixedRate = POST_VIEW_COUNT_SYNC_PERIOD)
	public void syncViews() {
		log.debug("조회 수 배치 스케줄러 작동");
		List<PostViewCountDto> views = postViewCountService.collectPostViewCounts();

		if (!views.isEmpty()) {
			postViewCountService.applyToDatabase(views);
		}
		log.debug("조회 수 DB 반영 완료");
	}
}
