package com.ndgl.spotfinder.domain.comment.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentDto;
import com.ndgl.spotfinder.domain.comment.dto.PostCommentReqDto;
import com.ndgl.spotfinder.domain.comment.service.PostCommentService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts/{id}/comments")
@Tag(name = "포스트 댓글", description = "포스트 댓글 관련 API")
@RequiredArgsConstructor
public class PostCommentController {
	private final PostCommentService postCommentService;

	@GetMapping
	@Operation(summary = "댓글 목록 조회", description = "포스트의 댓글 목록을 조회합니다.")
	public RsData<SliceResponse<PostCommentDto>> getComments(
		@PathVariable Long id,
		@ModelAttribute SliceRequest request
	) {
		return RsData.success(HttpStatus.OK, postCommentService.getComments(id, request.lastId(), request.size()));
	}

	@GetMapping("/{commentId}")
	@Operation(summary = "댓글 조회", description = "포스트별 댓글 id에 해당하는 댓글을 조회합니다.")
	public RsData<PostCommentDto> getComment(@PathVariable Long id, @PathVariable Long commentId) {
		return RsData.success(HttpStatus.OK, postCommentService.getComment(id, commentId));
	}

	@PostMapping
	@Operation(summary = "댓글 작성", description = "댓글을 작성합니다.")
	public RsData<Void> write(
		@PathVariable Long id,
		@RequestBody @Valid PostCommentReqDto reqBody,
		Principal principal
	) {
		postCommentService.write(id, reqBody, principal.getName());
		return RsData.success(HttpStatus.OK);
	}

	@PutMapping("/{commentId}")
	@Operation(summary = "댓글 수정", description = "댓글 수정, 작성자 이외 불가능")
	public RsData<Void> modify(
		@PathVariable Long id,
		@PathVariable Long commentId,
		@RequestBody @Valid PostCommentReqDto reqBody
	) {
		postCommentService.modify(id, commentId, reqBody.content());
		return RsData.success(HttpStatus.OK);
	}

	@DeleteMapping("/{commentId}")
	@Operation(summary = "댓글 삭제", description = "댓글 삭제, 작성자 이외 불가능")
	public RsData<Void> delete(@PathVariable Long id, @PathVariable Long commentId) {
		postCommentService.delete(id, commentId);
		return RsData.success(HttpStatus.OK);
	}
}
