package com.ndgl.spotfinder.domain.like.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/like")
public class LikeController implements LikeApiSpecification {

	private final LikeService likeService;

	/**
	 * 댓글 좋아요 토글
	 */
	@PostMapping("/comments/{commentId}")
	public RsData<Boolean> toggleCommentLike(
		@Positive @PathVariable Long commentId,
		Principal principal
	) {
		boolean isAdded = likeService.toggleLike(principal.getName(), commentId, Like.TargetType.COMMENT);
		return RsData.success(HttpStatus.OK, isAdded);
	}

	/**
	 * 포스트 좋아요 토글
	 */
	@PostMapping("/posts/{postId}")
	public RsData<Boolean> togglePostLike(
		@Positive @PathVariable Long postId,
		Principal principal
	) {
		boolean isAdded = likeService.toggleLike(principal.getName(), postId, Like.TargetType.POST);
		return RsData.success(HttpStatus.OK, isAdded);
	}

}
