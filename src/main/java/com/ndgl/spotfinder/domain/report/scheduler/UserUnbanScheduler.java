package com.ndgl.spotfinder.domain.report.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.report.entity.Ban;
import com.ndgl.spotfinder.domain.report.repository.BanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserUnbanScheduler {

	private final BanRepository banRepository;

	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void updateExpiredBans() {
		LocalDate today = LocalDate.now();
		List<Ban> expiredBans = banRepository.findBansWithExpiredDate(today);
		for (Ban ban : expiredBans) {
			ban.getUser().setBanned(false);
		}
		log.info("Updated {} users with expired bans", expiredBans.size());
	}
}
