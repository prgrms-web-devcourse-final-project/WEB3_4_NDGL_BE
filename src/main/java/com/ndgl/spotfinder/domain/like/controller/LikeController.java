package com.ndgl.spotfinder.domain.like.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Tag(name = "좋아요 API", description = "좋아요 관련 API")
public class LikeController {

	private final LikeService likeService;

	/**
	 * 댓글 좋아요
	 */
	@PostMapping("/comments/{commentId}")
	@Operation(
		summary = "댓글 좋아요", 
		description = "누른 댓글의 좋아요를 추가, 삭제합니다. true는 추가, false는 삭제를 의미합니다.",
		security = { @SecurityRequirement(name = "JWT") }
	)
	public RsData<Boolean> addCommentLike(
		@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
		@Parameter(description = "댓글 ID", example = "1") @PathVariable Integer commentId
	) {
		long userId = ((CustomUserDetails) userDetails).getUser().getId();
		boolean isAdded = likeService.toggleCommentLike(userId, commentId);
		return RsData.success(HttpStatus.OK, isAdded);
	}

	/**
	 * 포스트 좋아요
	 */
	@PostMapping("/posts/{postId}")
	@Operation(
		summary = "포스트 좋아요", 
		description = "누른 포스트의 좋아요를 추가, 삭제합니다. true는 추가, false는 삭제를 의미합니다.",
		security = { @SecurityRequirement(name = "JWT") }
	)
	public RsData<Boolean> addPostLike(
		@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
		@Parameter(description = "포스트 ID", example = "1") @PathVariable Integer postId
	) {
		long userId = ((CustomUserDetails) userDetails).getUser().getId();
		boolean isAdded = likeService.togglePostLike(userId, postId);
		return RsData.success(HttpStatus.OK, isAdded);
	}

}
