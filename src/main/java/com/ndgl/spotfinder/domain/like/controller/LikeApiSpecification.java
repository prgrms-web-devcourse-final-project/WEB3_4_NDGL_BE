package com.ndgl.spotfinder.domain.like.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "좋아요 API", description = "좋아요 관련 API")
public interface LikeApiSpecification {
	@PostMapping("/comments/{commentId}")
	@Operation(
		summary = "댓글 좋아요",
		description = "누른 댓글의 좋아요를 추가, 삭제합니다. true는 추가, false는 삭제를 의미합니다.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<Boolean> addCommentLike(
		@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
		@Parameter(description = "댓글 ID", example = "1") @PathVariable Integer commentId
	);

	@PostMapping("/posts/{postId}")
	@Operation(
		summary = "포스트 좋아요",
		description = "누른 포스트의 좋아요를 추가, 삭제합니다. true는 추가, false는 삭제를 의미합니다.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<Boolean> addPostLike(
		@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
		@Parameter(description = "포스트 ID", example = "1") @PathVariable Integer postId
	);
}
