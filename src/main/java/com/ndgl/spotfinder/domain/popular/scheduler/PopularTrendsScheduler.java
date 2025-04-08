package com.ndgl.spotfinder.domain.popular.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.domain.popular.dto.KeywordCount;
import com.ndgl.spotfinder.domain.popular.dto.PostCount;
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
	// @Scheduled(cron = "0 17 14 * * *") // 매일 00:00부터 30분 간격으로 실행
	public void updatePopularTrends() {
		log.info("인기 검색어 및 게시물 업데이트 스케줄러 시작: {}", LocalDateTime.now());

		try {
			long endTime = System.currentTimeMillis();
			long startTime = endTime - (30 * 60 * 1000);

			// Elasticsearch 에서 인기 검색어 / 인기 포스트 조회
			List<KeywordCount> topKeywords = elasticsearchPopularService.findTopKeywords(startTime, endTime, TOP_SIZE);
			List<PostCount> topPosts = elasticsearchPopularService.findTopPosts(startTime, endTime, TOP_SIZE);

			// 레디스 업데이트
			redisPopularService.updateRedisPopularKeywords(topKeywords);
			redisPopularService.updateRedisPopularPosts(topPosts);

			// MySQL 업데이트
			popularService.savePopularKeywords(topKeywords);
			popularService.savePopularPosts(topPosts);

			// 레디스 조회
			// TODO: 안정화되면 지워야 함
			List<KeywordCount> keywordCounts = redisPopularService.getPopularKeywords(10);
			List<PostCount> postCounts = redisPopularService.getPopularPosts(10);

			for(int i=0; i< keywordCounts.size(); i++) {
				log.info("{}위 - 인기 검색어 : {}, 인기 키워드 : {}", i+1, keywordCounts.get(i).keyword(), postCounts.get(i).postId());
			}

			log.info("인기 검색어 및 게시물 업데이트 완료");
		} catch (Exception e) {
			log.error("인기 트렌드 업데이트 중 오류 발생", e);
		}
	}
}
