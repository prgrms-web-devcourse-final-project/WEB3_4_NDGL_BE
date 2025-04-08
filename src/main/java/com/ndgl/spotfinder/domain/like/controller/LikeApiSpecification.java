package com.ndgl.spotfinder.domain.like.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "좋아요 API", description = "좋아요 관련 API")
public interface LikeApiSpecification {
	@PostMapping("/comments/{commentId}")
	@Operation(
		summary = "댓글 좋아요 토글",
		description = "댓글의 좋아요 상태를 토글합니다. 반환값 true는 좋아요 추가, false는 좋아요 취소를 의미합니다.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<Boolean> toggleCommentLike(
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails customUserDetails,
		@Parameter(description = "댓글 ID", example = "1") @PathVariable Integer commentId
	);

	@PostMapping("/posts/{postId}")
	@Operation(
		summary = "포스트 좋아요 토글",
		description = "포스트의 좋아요 상태를 토글합니다. 반환값 true는 좋아요 추가, false는 좋아요 취소를 의미합니다.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<Boolean> togglePostLike(
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails customUserDetails,
		@Parameter(description = "포스트 ID", example = "1") @PathVariable Integer postId
	);
}
