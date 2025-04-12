package com.ndgl.spotfinder.domain.blog.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record BlogResponseDto(
	@Schema(description = "ID", example = "1")
	Long id,

	@Schema(description = "블로그 이름", example = "맛집사냥꾼의 블로그")
	String blogName,

	@Schema(description = "닉네임", example = "맛집사냥꾼")
	String nickname,

	@Schema(description = "팔로우 여부", example = "false")
	Boolean isFollowed,

	@Schema(description = "포스트 목록")
	List<PostSummeryDto> posts
) {
}
