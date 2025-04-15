package com.ndgl.spotfinder.domain.popular;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ndgl.spotfinder.domain.popular.dto.KeywordCountDto;
import com.ndgl.spotfinder.domain.popular.dto.PostCountDto;
import com.ndgl.spotfinder.domain.popular.entity.PopularKeyword;
import com.ndgl.spotfinder.domain.popular.entity.PopularPost;
import com.ndgl.spotfinder.domain.popular.repository.PopularKeywordRepository;
import com.ndgl.spotfinder.domain.popular.repository.PopularPostRepository;
import com.ndgl.spotfinder.domain.popular.service.PopularService;

@ExtendWith(MockitoExtension.class)
class PopularServiceTest {
	@Mock
	PopularPostRepository popularPostRepository;

	@Mock
	PopularKeywordRepository popularKeywordRepository;

	@InjectMocks
	PopularService popularService;

	@Test
	@DisplayName("인기 키워드 저장 테스트")
	void savePopularKeywords_success() {
		// given
		List<KeywordCountDto> keywords = List.of(
			new KeywordCountDto("서울", 100L),
			new KeywordCountDto("강남", 80L)
		);

		// when
		popularService.savePopularKeywords(keywords);

		// then
		ArgumentCaptor<List<PopularKeyword>> captor = ArgumentCaptor.forClass(List.class);
		verify(popularKeywordRepository, times(1)).saveAll(captor.capture());

		List<PopularKeyword> saved = captor.getValue();
		assertThat(saved).hasSize(2);
		assertThat(saved.get(0).getKeyword()).isEqualTo("서울");
		assertThat(saved.get(0).getSearchCount()).isEqualTo(100L);
		assertThat(saved.get(0).getRanking()).isEqualTo(1);

		assertThat(saved.get(1).getKeyword()).isEqualTo("강남");
		assertThat(saved.get(1).getSearchCount()).isEqualTo(80L);
		assertThat(saved.get(1).getRanking()).isEqualTo(2);
	}

	@Test
	@DisplayName("인기 게시물 저장 테스트")
	void savePopularPosts_success() {
		// given
		List<PostCountDto> posts = List.of(
			new PostCountDto(1L, 100L),
			new PostCountDto(2L, 80L)
		);

		// when
		popularService.savePopularPosts(posts);

		// then
		ArgumentCaptor<List<PopularPost>> captor = ArgumentCaptor.forClass(List.class);
		verify(popularPostRepository, times(1)).saveAll(captor.capture());

		List<PopularPost> saved = captor.getValue();
		assertThat(saved).hasSize(2);
		assertThat(saved.get(0).getPostId()).isEqualTo(1L);
		assertThat(saved.get(0).getViewCount()).isEqualTo(100L);
		assertThat(saved.get(0).getRanking()).isEqualTo(1);

		assertThat(saved.get(1).getPostId()).isEqualTo(2L);
		assertThat(saved.get(1).getViewCount()).isEqualTo(80L);
		assertThat(saved.get(1).getRanking()).isEqualTo(2);
	}
}
