package com.ndgl.spotfinder.domain.popular.elasticsearch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ndgl.spotfinder.domain.popular.dto.KeywordCountDto;
import com.ndgl.spotfinder.domain.popular.dto.PostCountDto;
import com.ndgl.spotfinder.domain.popular.service.elasticsearch.ElasticsearchPopularService;
import com.ndgl.spotfinder.global.app.AppConfig;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;

@ExtendWith(MockitoExtension.class)
class ElasticsearchServiceTest {

	@Mock
	private AppConfig appConfig;

	@Mock
	private ElasticsearchClient elasticsearchClient;

	@InjectMocks
	private ElasticsearchPopularService elasticsearchPopularService;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private ZonedDateTime now;
	private long currentTimestamp;

	@BeforeEach
	void setup() {
		now = ZonedDateTime.now(ZoneId.systemDefault());
		currentTimestamp = now.toInstant().toEpochMilli();
	}

	@Test
	@DisplayName("인기 검색어 조회 - 단일 날짜")
	void 단일_날짜에서_TOP_N개_인기_검색어_조회() throws Exception {
		//given
		int size = 5;
		long startTime = currentTimestamp - 30 * 60 * 1000;
		long endTime = currentTimestamp;

		SearchResponse<Void> mockResponse = mockKeywordSearchResponse();
		// search()
		when(elasticsearchClient.search(any(Function.class), eq(Void.class))).thenReturn(mockResponse);

		// when
		List<KeywordCountDto> result = elasticsearchPopularService.findTopKeywords(startTime, endTime, size);

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).keyword()).isEqualTo("spring");
		assertThat(result.get(0).count()).isEqualTo(100L);
		assertThat(result.get(1).keyword()).isEqualTo("java");
		assertThat(result.get(1).count()).isEqualTo(80L);
		assertThat(result.get(2).keyword()).isEqualTo("elasticsearch");
		assertThat(result.get(2).count()).isEqualTo(50L);
	}

	@Test
	@DisplayName("인기 게시물 조회 - 단일 날짜")
	void 단일_날짜에서_TOP_N개_인기_게시물_조회() throws Exception {
		//given
		int size = 5;
		long startTime = currentTimestamp - 30 * 60 * 1000;
		long endTime = currentTimestamp;

		SearchResponse<Void> mockResponse = mockPostSearchResponse();
		// search()
		when(elasticsearchClient.search(any(Function.class), eq(Void.class))).thenReturn(mockResponse);

		// when.
		List<PostCountDto> result = elasticsearchPopularService.findTopPosts(startTime, endTime, size);

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).postId()).isEqualTo(1L);
		assertThat(result.get(0).count()).isEqualTo(100L);
		assertThat(result.get(1).postId()).isEqualTo(2L);
		assertThat(result.get(1).count()).isEqualTo(80L);
		assertThat(result.get(2).postId()).isEqualTo(3L);
		assertThat(result.get(2).count()).isEqualTo(50L);
	}

	private SearchResponse<Void> mockKeywordSearchResponse() {
		SearchResponse<Void> mockResponse = mock(SearchResponse.class);
		Map<String, Aggregate> aggregationMap = new HashMap<>();

		StringTermsAggregate keywordAggregate = mock(StringTermsAggregate.class);
		List<StringTermsBucket> buckets = List.of(
			createStringTermsBucket("spring", 100L),
			createStringTermsBucket("java", 80L),
			createStringTermsBucket("elasticsearch", 50L)
		);

		Buckets mockBuckets = mock(Buckets.class);

		// array()
		when(mockBuckets.array()).thenReturn(buckets);
		// buckets()
		when(keywordAggregate.buckets()).thenReturn(mockBuckets);
		// terms()
		Aggregate topKeywordsAggregate = Aggregate.of(a -> a.sterms(keywordAggregate));
		// get()
		aggregationMap.put("top_keywords", topKeywordsAggregate);
		// aggregations()
		when(mockResponse.aggregations()).thenReturn(aggregationMap);

		return mockResponse;
	}

	private StringTermsBucket createStringTermsBucket(String key, long docCount) {
		StringTermsBucket bucket = mock(StringTermsBucket.class);
		FieldValue mockKey = mock(FieldValue.class);

		// key()
		when(bucket.key()).thenReturn(mockKey);
		// stringValue()
		when(mockKey.stringValue()).thenReturn(key);
		// docCount()
		when(bucket.docCount()).thenReturn(docCount);
		return bucket;
	}


	private SearchResponse<Void> mockPostSearchResponse() {
		SearchResponse<Void> mockResponse = mock(SearchResponse.class);
		Map<String, Aggregate> aggregationMap = new HashMap<>();

		LongTermsAggregate postAggregate = mock(LongTermsAggregate.class);
		List<LongTermsBucket> buckets = List.of(
			createLongTermsBucket(1L, 100L),
			createLongTermsBucket(2L, 80L),
			createLongTermsBucket(3L, 50L)
		);

		Buckets mockBuckets = mock(Buckets.class);

		// array()
		when(mockBuckets.array()).thenReturn(buckets);
		// buckets()
		when(postAggregate.buckets()).thenReturn(mockBuckets);
		// terms()
		Aggregate topPostsAggregate = Aggregate.of(a -> a.lterms(postAggregate));
		// get()
		aggregationMap.put("top_posts", topPostsAggregate);
		// aggregations()
		when(mockResponse.aggregations()).thenReturn(aggregationMap);

		return mockResponse;
	}

	private LongTermsBucket createLongTermsBucket(long key, long docCount) {
		LongTermsBucket bucket = mock(LongTermsBucket.class);
		// key()
		when(bucket.key()).thenReturn(key);
		// docCount()
		when(bucket.docCount()).thenReturn(docCount);
		return bucket;
	}

}
