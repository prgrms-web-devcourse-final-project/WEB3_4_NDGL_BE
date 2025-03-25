package com.ndgl.spotfinder.domain.like.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {

	private final LikeService likeService;

	// TODO: 유저 기능 안정화 후 실제 사용자 인증 방식으로 변경
	private Long getCurrentUserId() {
		return 1L;  // 임시로 1을 반환
	}

	/**
	 * 댓글 좋아요 추가
	 */
	@PostMapping("/comments/{commentId}")
	public RsData<Void> addCommentLike(@PathVariable Long commentId) {
		long userId = getCurrentUserId();
		likeService.addCommentLike(userId, commentId);
		return RsData.success(HttpStatus.OK, null);
	}

	/**
	 * 댓글 좋아요 취소
	 */
	@DeleteMapping("/comments/{commentId}")
	public RsData<Void> removeCommentLike(@PathVariable Long commentId) {
		long userId = getCurrentUserId();
		likeService.deleteCommentLike(userId, commentId);
		return RsData.success(HttpStatus.OK, null);
	}

	/**
	 * 포스트 좋아요 추가
	 */
	@PostMapping("/posts/{postId}")
	public RsData<Void> addPostLike(@PathVariable Long postId) {
		long userId = getCurrentUserId();
		likeService.addPostLike(userId, postId);
		return RsData.success(HttpStatus.OK, null);
	}

	/**
	 * 포스트 좋아요 취소
	 */
	@DeleteMapping("/posts/{postId}")
	public RsData<Void> removePostLike(@PathVariable Long postId) {
		long userId = getCurrentUserId();
		likeService.deletePostLike(userId, postId);
		return RsData.success(HttpStatus.OK, null);
	}

}
