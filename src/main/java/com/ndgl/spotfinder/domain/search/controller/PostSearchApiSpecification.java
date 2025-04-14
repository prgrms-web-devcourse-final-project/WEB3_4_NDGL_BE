package com.ndgl.spotfinder.domain.search.controller;

import java.util.List;

import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "포스트 검색", description = "포스트 검색 관련 API")
public interface PostSearchApiSpecification {
	@Operation(
		summary = "포스트 검색",
		description = "검색어에 해당하는 포스트를 검색합니다."
	)
	RsData<SliceResponse<PostResponseDto>> searchPosts(
		SliceRequest sliceRequest,
		@Parameter(description = "검색 키워드") String keyword
	);

	@Operation(
		summary = "포스트 검색 자동완성",
		description = "검색어 자동완성 리스트를 반환합니다."
	)
	RsData<List<String>> suggestKeyword(
		@Parameter(description = "검색 키워드") String keyword
	);
}
