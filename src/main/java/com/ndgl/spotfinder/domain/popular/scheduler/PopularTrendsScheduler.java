package com.ndgl.spotfinder.domain.popular.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.domain.popular.dto.KeywordCountDto;
import com.ndgl.spotfinder.domain.popular.dto.PostCountDto;
import com.ndgl.spotfinder.domain.popular.service.PopularService;
import com.ndgl.spotfinder.domain.popular.service.elasticsearch.ElasticsearchPopularService;
import com.ndgl.spotfinder.domain.popular.service.redis.RedisPopularService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularTrendsScheduler {

	private static final int TOP_SIZE = 10;
	private final ElasticsearchPopularService elasticsearchPopularService;
	private final RedisPopularService redisPopularService;
	private final PopularService popularService;

	// TODO: 안정화 되면 가동 시작
	@Scheduled(cron = "0 */3 * * * *") // 매일 00:00부터 30분 간격으로 실행
	public void updatePopularTrends() {
		log.info("인기 검색어 및 게시물 업데이트 스케줄러 시작: {}", LocalDateTime.now());

		try {
			long endTime = System.currentTimeMillis();
			long startTime = endTime - (3 * 60 * 1000);

			// Elasticsearch 에서 인기 검색어 / 인기 포스트 조회
			List<KeywordCountDto> topKeywords = elasticsearchPopularService.findTopKeywords(startTime, endTime, TOP_SIZE);
			List<PostCountDto> topPosts = elasticsearchPopularService.findTopPosts(startTime, endTime, TOP_SIZE);

			// 레디스 업데이트
			redisPopularService.updateRedisPopularKeywords(topKeywords);
			redisPopularService.updateRedisPopularPosts(topPosts);

			// MySQL 업데이트
			popularService.savePopularKeywords(topKeywords);
			popularService.savePopularPosts(topPosts);

			// 레디스 조회
			// TODO: 안정화되면 지워야 함
			List<KeywordCountDto> keywordCountDtos = redisPopularService.getPopularKeywords(10);
			List<PostCountDto> postCountDtos = redisPopularService.getPopularPosts(10);

			for(int i = 0; i< keywordCountDtos.size(); i++) {
				log.info("{}위 - 인기 검색어 : {} -{}",
					i+1, keywordCountDtos.get(i).keyword(), keywordCountDtos.get(i).count());
			}

			for(int i = 0; i< postCountDtos.size(); i++) {
				log.info("{}위 - 인기 게시물 : {} -{}",
					i+1, postCountDtos.get(i).postId(), postCountDtos.get(i).count());
			}

			log.info("인기 검색어 및 게시물 업데이트 완료");
		} catch (Exception e) {
			log.error("인기 트렌드 업데이트 중 오류 발생", e);
		}
	}
}
