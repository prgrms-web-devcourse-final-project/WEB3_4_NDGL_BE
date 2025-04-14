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

import com.ndgl.spotfinder.domain.comment.dto.PostCommentRequestDto;
import com.ndgl.spotfinder.domain.comment.dto.PostCommentResponseDto;
import com.ndgl.spotfinder.domain.comment.service.PostCommentService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.util.Ut;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts/{id}/comments")
@RequiredArgsConstructor
public class PostCommentController implements PostCommentApiSpecification {
	private final PostCommentService postCommentService;

	@GetMapping
	public RsData<SliceResponse<PostCommentResponseDto>> getComments(
		@PathVariable Long id,
		@ModelAttribute SliceRequest request,
		Principal principal
	) {
		return RsData.success(HttpStatus.OK,
			postCommentService.getComments(Ut.getEmail(principal), id, request.lastId(), request.size())
		);
	}

	@GetMapping("/{commentId}")
	public RsData<PostCommentResponseDto> getComment(
		@PathVariable Long id,
		@PathVariable Long commentId,
		Principal principal
	) {
		return RsData.success(HttpStatus.OK, postCommentService.getComment(Ut.getEmail(principal), id, commentId));
	}

	@PostMapping
	public RsData<Void> write(
		@PathVariable Long id,
		@RequestBody @Valid PostCommentRequestDto reqBody,
		Principal principal
	) {
		postCommentService.write(id, reqBody, principal.getName());
		return RsData.success(HttpStatus.OK);
	}

	@PutMapping("/{commentId}")
	public RsData<Void> modify(
		@PathVariable Long id,
		@PathVariable Long commentId,
		@RequestBody @Valid PostCommentRequestDto reqBody,
		Principal principal
	) {
		postCommentService.modify(id, commentId, reqBody.content(), principal.getName());
		return RsData.success(HttpStatus.OK);
	}

	@DeleteMapping("/{commentId}")
	public RsData<Void> delete(
		@PathVariable Long id,
		@PathVariable Long commentId,
		Principal principal
	) {
		postCommentService.delete(id, commentId, principal.getName());
		return RsData.success(HttpStatus.OK);
	}

	@PutMapping("/{commentId}/pin")
	public RsData<Void> pin(
		@PathVariable Long id,
		@PathVariable Long commentId,
		Principal principal
	) {
		postCommentService.pinComment(id, commentId, principal.getName());
		return RsData.success(HttpStatus.OK);
	}
}
