package com.ndgl.spotfinder.domain.search.repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.search.document.PostDocument;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;

@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
@Repository
@RequiredArgsConstructor
public class PostSearchRepositoryImpl implements PostSearchRepositoryCustom {
	private final ElasticsearchClient elasticsearchClient;

	@Override
	public Page<PostDocument> searchByKeyword(String keyword, Pageable pageable) {
		int size = pageable.getPageSize();
		int from = pageable.getPageNumber() * size;

		Query functionScoreQuery = Query.of(q -> q
			.functionScore(fs -> fs
				.query(q1 -> q1
					.bool(b -> b
						.should(s -> s.match(m -> m.field("title").query(keyword)))
						.should(s -> s.match(m -> m.field("content").query(keyword)))
						.should(s -> s.match(m -> m.field("nickname").query(keyword)))
						.should(s -> s.match(m -> m.field("hashtags").query(keyword)))
					)
				)
				.functions(f -> f
					.fieldValueFactor(fvf -> fvf
						.field("viewCount")
						.factor(0.5)
						.modifier(FieldValueFactorModifier.Log1p)
						.missing(0.0)
					)
				)
				.functions(f -> f
					.fieldValueFactor(fvf -> fvf
						.field("likeCount")
						.factor(1.0)
						.modifier(FieldValueFactorModifier.Log1p)
						.missing(0.0)
					)
				)
				.scoreMode(FunctionScoreMode.Sum)
			)
		);

		SearchResponse<PostDocument> response;
		try {
			response = elasticsearchClient.search(s -> s
					.index("post_index")
					.from(from)
					.size(size)
					.query(functionScoreQuery),
				PostDocument.class
			);
		} catch (IOException e) {
			throw ErrorCode.SEARCH_FAIL.throwServiceException(e);
		}

		List<PostDocument> content = response.hits().hits().stream()
			.map(Hit::source)
			.filter(Objects::nonNull)
			.toList();

		long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;

		return new PageImpl<>(content, pageable, totalHits);
	}

	@Override
	public List<String> suggestKeyword(String keyword) {
		try {
			SearchResponse<PostDocument> response = elasticsearchClient.search(s -> s
					.index("post_index")
					.size(30)
					.query(q -> q
						.multiMatch(m -> m
							.query(keyword)
							.fields("title", "content", "nickname", "hashtags")
							.type(TextQueryType.PhrasePrefix) // 검색어 자동완성
						)
					),
				PostDocument.class
			);

			return response.hits().hits().stream()
				.map(Hit::source)
				.filter(Objects::nonNull)
				.flatMap(doc -> Stream.concat(
					Stream.of(
							Optional.ofNullable(doc.getTitle()),
							Optional.ofNullable(doc.getContent()),
							Optional.ofNullable(doc.getNickname())
						)
						.flatMap(Optional::stream)
						.filter(s -> !s.isBlank()),
					doc.getHashtags() != null ? doc.getHashtags().stream() : Stream.empty()
				))
				.flatMap(text -> Arrays.stream(text.split("[\\s\\p{Punct}]+")))
				.map(String::trim)
				.filter(word -> !word.isBlank())
				.filter(word -> word.startsWith(keyword))
				.distinct()
				.sorted(String::compareTo)
				.limit(10) // 상위 10개까지 반환
				.toList();

		} catch (IOException e) {
			throw ErrorCode.KEYWORD_SUGGESTION_FAIL.throwServiceException(e);
		}
	}
}
