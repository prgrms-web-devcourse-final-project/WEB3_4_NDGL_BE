package com.ndgl.spotfinder.domain.comment.controller;

import java.security.Principal;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentRequestDto;
import com.ndgl.spotfinder.domain.comment.dto.PostCommentResponseDto;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "포스트 댓글")
public interface PostCommentApiSpecification {
	@Operation(
		summary = "포스트별 댓글 목록 조회",
		description = "포스트의 댓글 목록을 조회합니다.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<SliceResponse<PostCommentResponseDto>> getComments(
		@Parameter(description = "게시물의 ID") Long id,
		SliceRequest sliceRequest,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(
		summary = "포스트 댓글 조회",
		description = "포스트별 댓글 id에 해당하는 댓글을 조회합니다.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<PostCommentResponseDto> getComment(
		@Parameter(description = "게시물의 ID") Long id,
		@Parameter(description = "댓글의 ID") Long commentId,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(
		summary = "포스트 댓글 작성",
		description = "댓글을 작성합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<Void> write(
		@Parameter(description = "게시물의 ID") Long id,
		PostCommentRequestDto reqBody,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(
		summary = "포스트 댓글 수정",
		description = "댓글 수정, 작성자 이외 불가능",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<Void> modify(
		@Parameter(description = "게시물의 ID") Long id,
		@Parameter(description = "댓글의 ID") Long commentId,
		PostCommentRequestDto reqBody,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(
		summary = "포스트 댓글 삭제",
		description = "댓글 삭제, 작성자 이외 불가능",
		security = {@SecurityRequirement(name = "JWT")},
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<Void> delete(
		@Parameter(description = "게시물의 ID") Long id,
		@Parameter(description = "댓글의 ID") Long commentId,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(
		summary = "포스트 댓글 고정",
		description = "댓글을 고정합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<Void> pin(
		@Parameter(description = "게시물의 ID") Long id,
		@Parameter(description = "댓글의 ID") Long commentId,
		@Parameter(hidden = true) Principal principal
	);
}
