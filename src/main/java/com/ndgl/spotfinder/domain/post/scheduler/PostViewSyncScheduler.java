package com.ndgl.spotfinder.domain.post.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.domain.post.dto.PostViewCountDto;
import com.ndgl.spotfinder.domain.post.service.PostViewCountService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostViewSyncScheduler {
	private final PostViewCountService postViewCountService;

	private static final long POST_VIEW_COUNT_SYNC_PERIOD = 60 * 1000;

	@Scheduled(fixedRate = POST_VIEW_COUNT_SYNC_PERIOD)
	public void syncViews() {
		List<PostViewCountDto> views = postViewCountService.collectPostViewCounts();

		if (!views.isEmpty()) {
			postViewCountService.applyToDatabase(views);
		}
	}
}
