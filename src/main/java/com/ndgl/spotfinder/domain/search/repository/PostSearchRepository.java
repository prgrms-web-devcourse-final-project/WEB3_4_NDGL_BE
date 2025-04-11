package com.ndgl.spotfinder.domain.search.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.search.document.PostDocument;

@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long>, PostSearchRepositoryCustom {
}
