package com.ndgl.spotfinder.domain.search.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.search.service.PostSearchService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts/search")
@RequiredArgsConstructor
public class PostSearchController {
	private final PostSearchService postSearchService;

	@GetMapping
	public RsData<SliceResponse<PostResponseDto>> searchPosts(
		@ModelAttribute @Valid SliceRequest sliceRequest,
		@RequestParam String keyword
	) {
		SliceResponse<PostResponseDto> results = postSearchService.searchPosts(sliceRequest, keyword);

		return RsData.success(HttpStatus.OK, results);
	}
}
