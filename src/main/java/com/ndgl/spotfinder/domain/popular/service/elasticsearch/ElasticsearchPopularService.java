package com.ndgl.spotfinder.domain.popular.service.elasticsearch;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.popular.dto.KeywordCountDto;
import com.ndgl.spotfinder.domain.popular.dto.PostCountDto;
import com.ndgl.spotfinder.global.app.AppConfig;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ElasticsearchPopularService {

	private final ElasticsearchClient elasticsearchClient;
	private final String keywordSearchIndex;
	private final String postViewIndex;

	@Autowired
	public ElasticsearchPopularService(ElasticsearchClient elasticsearchClient, AppConfig appConfig) {
		this.elasticsearchClient = elasticsearchClient;

		String env = appConfig.getActiveProfile();
		this.keywordSearchIndex = env + "-search-";
		this.postViewIndex = env + "-post-view-";
	}

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");


	// 인기 검색어 Top N 조회
	public List<KeywordCountDto> findTopKeywords(long startTime, long endTime, int size) throws IOException {
		String[] indices = getIndicesForTimeRange(startTime, endTime, keywordSearchIndex);

		for (String index : indices) {
			log.info("Elasticsearch 조회할 검색 인덱스 : {}", index);
		}

		JsonData startTimeJson = JsonData.of(timestampToISO8601String(startTime));
		JsonData endTimeJson = JsonData.of(timestampToISO8601String(endTime));
		log.info("Elasticsearch 조회할 시간 범위 : {} ~ {}", startTimeJson.toString(), endTimeJson.toString());

		SearchResponse<Void> response = elasticsearchClient.search(builder -> builder
				.index(List.of(indices)) // 여러 인덱스 검색
				.query(q -> q
					.range(r -> r
						.field("@timestamp")
						.gte(startTimeJson)
						.lte(endTimeJson)
					)
				)
				.aggregations("top_keywords", a -> a
					.terms(t -> t
						.field("keyword.joined")
						.size(size)
					)
				)
				.size(0), // 집계 결과만 필요하므로 검색 결과는 불필요
			Void.class
		);

		List<KeywordCountDto> result = new ArrayList<>();
		List<StringTermsBucket> buckets = response.aggregations().get("top_keywords").sterms().buckets().array();

		for(StringTermsBucket bucket : buckets) {
			result.add(new KeywordCountDto(bucket.key().stringValue(), bucket.docCount()));
		}

		log.info("Elasticsearch 인기 검색어 조회 완료: {} 개 키워드", buckets.size());

		return result;
	}

	// 인기 포스트 Top N 조회
	public List<PostCountDto> findTopPosts(long startTime, long endTime, int size) throws IOException {
		String[] indices = getIndicesForTimeRange(startTime, endTime, postViewIndex);

		for (String index : indices) {
			log.info("Elasticsearch 조회할 게시물 인덱스 : {}", index);
		}

		JsonData startTimeJson = JsonData.of(timestampToISO8601String(startTime));
		JsonData endTimeJson = JsonData.of(timestampToISO8601String(endTime));
		log.info("Elasticsearch 조회할 시간 범위 : {} ~ {}", startTimeJson.toString(), endTimeJson.toString());

		SearchResponse<Void> response = elasticsearchClient.search(builder -> builder
				.index(List.of(indices)) // 여러 인덱스 검색
				.query(q -> q
					.range(r -> r
						.field("@timestamp")
						.gte(startTimeJson)
						.lte(endTimeJson)
					)
				)
				.aggregations("top_posts", a -> a
					.terms(t -> t
						.field("postId")
						.size(size)
					)
				)
				.size(0), // 집계 결과만 필요하므로 검색 결과는 불필요
			Void.class
		);

		List<PostCountDto> result = new ArrayList<>();
		List<LongTermsBucket> buckets = response.aggregations().get("top_posts").lterms().buckets().array();

		for(LongTermsBucket bucket : buckets) {
			result.add(new PostCountDto(bucket.key(), bucket.docCount()));
		}

		log.info("Elasticsearch 인기 게시물 조회 완료: {} 개 키워드", buckets.size());

		return result;
	}

	// startTime ~ endTime 사이에 날짜가 변경 된다면 2일에 걸친 인덱스 모두 활용해야 함
	private String[] getIndicesForTimeRange(long startTime, long endTime, String indexPrefix) {
		String startDateStr = timestampToCurrentDateString(startTime);
		String endDateStr = timestampToCurrentDateString(endTime);

		if (startDateStr.equals(endDateStr)) {
			return new String[] {indexPrefix + startDateStr};
		}

		return new String[] {indexPrefix + startDateStr, indexPrefix + endDateStr};
	}

	// epoch 를 yyyy-MM-dd 날짜 문자열로 변경
	private String timestampToCurrentDateString(long timeStamp) {
		LocalDate date = Instant.ofEpochMilli(timeStamp)
			.atZone(ZoneId.systemDefault())
			.toLocalDate();
		return date.format(DATE_FORMATTER);
	}

	// epoch 를 Elasticsearch에서 사용하는 ISO 8601 형식으로 변환
	private String timestampToISO8601String(long timestamp) {
		return Instant.ofEpochMilli(timestamp)
			.atZone(ZoneId.systemDefault())
			.toOffsetDateTime() // 없으면 Asia/Seoul 이 포함돼서 에러 발생
			.toString();
	}
}
