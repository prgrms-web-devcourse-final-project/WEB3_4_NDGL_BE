package com.ndgl.spotfinder.domain.popular.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.popular.dto.KeywordCountDto;
import com.ndgl.spotfinder.domain.popular.dto.PostCountDto;
import com.ndgl.spotfinder.domain.popular.entity.PopularKeyword;
import com.ndgl.spotfinder.domain.popular.entity.PopularPost;
import com.ndgl.spotfinder.domain.popular.repository.PopularKeywordRepository;
import com.ndgl.spotfinder.domain.popular.repository.PopularPostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularService {

	private final PopularKeywordRepository popularKeywordRepository;
	private final PopularPostRepository popularPostRepository;

	public void savePopularKeywords(List<KeywordCountDto> keywords) {
		List<PopularKeyword> entities = new ArrayList<>();

		for (int i = 0; i < keywords.size(); i++) {
			KeywordCountDto kc = keywords.get(i);
			entities.add(PopularKeyword.builder()
				.keyword(kc.keyword())
				.searchCount(kc.count())
				.rank(i + 1)
				.build());
		}

		popularKeywordRepository.saveAll(entities);
		log.info("MySQL 인기 검색어 저장 완료");
	}

	public void savePopularPosts(List<PostCountDto> posts) {
		List<PopularPost> entities = new ArrayList<>();

		for (int i = 0; i < posts.size(); i++) {
			PostCountDto pc = posts.get(i);
			entities.add(PopularPost.builder()
				.postId(pc.postId())
				.viewCount(pc.count())
				.rank(i + 1)
				.build());
		}

		popularPostRepository.saveAll(entities);
		log.info("MySQL 인기 게시물 저장 완료");
	}
}
