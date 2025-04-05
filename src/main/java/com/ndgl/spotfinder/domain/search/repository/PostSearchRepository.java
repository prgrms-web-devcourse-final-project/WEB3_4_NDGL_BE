package com.ndgl.spotfinder.domain.search.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.ndgl.spotfinder.domain.search.document.PostDocument;

@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {
	Page<PostDocument> findByTitleOrContent(String title, String content, Pageable pageable);
}
