package com.ndgl.spotfinder.domain.blog.controller;

import java.security.Principal;

import com.ndgl.spotfinder.domain.blog.dto.BlogResponseDto;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "블로그")
public interface BlogApiSpecification {
	@Operation(summary = "블로그 목록")
	RsData<SliceResponse<BlogResponseDto>> getAllBlogs(
		SliceRequest sliceRequest,
		@Parameter(hidden = true) Principal principal
	);
}
