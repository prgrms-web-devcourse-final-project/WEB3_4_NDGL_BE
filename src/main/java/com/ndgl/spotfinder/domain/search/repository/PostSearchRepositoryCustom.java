package com.ndgl.spotfinder.domain.search.repository;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.search.document.PostDocument;

@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
@Repository
public interface PostSearchRepositoryCustom {
	Page<PostDocument> searchByKeyword(String keyword, Pageable pageable);

	List<PostDocument> searchAllByKeyword(String keyword);
}
