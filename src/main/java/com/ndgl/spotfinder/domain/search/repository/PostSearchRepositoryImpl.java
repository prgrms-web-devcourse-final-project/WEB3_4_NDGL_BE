package com.ndgl.spotfinder.domain.search.repository;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.search.document.PostDocument;

import lombok.RequiredArgsConstructor;

@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
@Repository
@RequiredArgsConstructor
public class PostSearchRepositoryImpl implements PostSearchRepositoryCustom {
	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public Page<PostDocument> searchByKeyword(String keyword, Pageable pageable) {
		Criteria criteria = new Criteria()
			.or(new Criteria("title").matches(keyword))
			.or(new Criteria("content").matches(keyword))
			.or(new Criteria("nickname").matches(keyword))
			.or(new Criteria("hashtags").matches(keyword));

		Query query = new CriteriaQuery(criteria, pageable);
		SearchHits<PostDocument> searchHits = elasticsearchOperations.search(query, PostDocument.class);

		List<PostDocument> results = searchHits.stream()
			.map(SearchHit::getContent)
			.toList();

		return new PageImpl<>(results, pageable, searchHits.getTotalHits());
	}
}
