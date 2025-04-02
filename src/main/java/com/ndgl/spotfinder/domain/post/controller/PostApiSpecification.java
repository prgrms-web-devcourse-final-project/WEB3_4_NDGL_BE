package com.ndgl.spotfinder.domain.post.controller;

import java.security.Principal;

import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostDetailResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface PostApiSpecification {
	@Operation(
		summary = "포스트 생성",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<String> createPost(
		PostCreateRequestDto postCreateRequestDto,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(
		summary = "포스트 수정",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<String> updatePost(
		@Parameter(description = "게시물의 ID") Long id,
		PostUpdateRequestDto postUpdateRequestDto,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(
		summary = "포스트 삭제",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<String> deletePost(
		@Parameter(description = "게시물의 ID") Long id,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(
		summary = "전체 포스트 조회",
		description = "요청한 사이즈만큼 최신순으로 조회"
	)
	RsData<SliceResponse<PostResponseDto>> getPosts(
		SliceRequest sliceRequest
	);

	@Operation(summary = "포스트 1건 조회")
	RsData<PostDetailResponseDto> getPost(
		@Parameter(description = "게시물의 ID") Long id
	);

	@Operation(
		summary = "사용자가 작성한 포스트 목록 조회",
		description = "요청한 사이즈만큼 최신순으로 조회"
	)
	RsData<SliceResponse<PostResponseDto>> getPostsByUserId(
		@Parameter(description = "사용자의 ID") Long userId,
		SliceRequest sliceRequest
	);
}
