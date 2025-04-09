package com.ndgl.spotfinder.domain.like.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
public class LikeController implements LikeApiSpecification {

	private final LikeService likeService;

	/**
	 * 댓글 좋아요 토글
	 */
	@PostMapping("/comments/{commentId}")
	public RsData<Boolean> toggleCommentLike(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable Integer commentId
	) {
		long userId = customUserDetails.getUser().getId();
		boolean isAdded = likeService.toggleLike(userId, commentId, Like.TargetType.COMMENT);
		return RsData.success(HttpStatus.OK, isAdded);
	}

	/**
	 * 포스트 좋아요 토글
	 */
	@PostMapping("/posts/{postId}")
	public RsData<Boolean> togglePostLike(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable Integer postId
	) {
		long userId = customUserDetails.getUser().getId();
		boolean isAdded = likeService.toggleLike(userId, postId, Like.TargetType.POST);
		return RsData.success(HttpStatus.OK, isAdded);
	}

}
