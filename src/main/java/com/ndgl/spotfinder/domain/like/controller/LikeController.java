package com.ndgl.spotfinder.domain.like.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * @author 한상훈
 */
@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Tag(name = "BookingController", description = "좋아요 관련 API")
public class LikeController {

	private final LikeService likeService;

	// TODO: 유저 기능 안정화 후 실제 사용자 인증 방식으로 변경
	private Long getCurrentUserId() {
		return 1L;  // 임시로 1을 반환
	}

	/**
	 * 댓글 좋아요
	 */
	@PostMapping("/comments/{commentId}")
	@Operation(summary = "댓글 좋아요", description = "누른 댓글의 좋아요를 추가, 삭제")
	public RsData<Boolean> addCommentLike(
		// @AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Integer commentId
	) {
		// long userId = Long.parseLong(userDetails.getUsername());
		long userId = getCurrentUserId();
		boolean isAdded = likeService.toggleCommentLike(userId, commentId);
		return RsData.success(HttpStatus.OK, isAdded);
	}

	/**
	 * 포스트 좋아요
	 */
	@PostMapping("/posts/{postId}")
	@Operation(summary = "포스트 좋아요", description = "누른 포스트의 좋아요를 추가, 삭제")
	public RsData<Boolean> addPostLike(@PathVariable Integer postId
	) {
		long userId = getCurrentUserId();
		boolean isAdded = likeService.togglePostLike(userId, postId);
		return RsData.success(HttpStatus.OK, isAdded);
	}

}
