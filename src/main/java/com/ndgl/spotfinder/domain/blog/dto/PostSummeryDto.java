package com.ndgl.spotfinder.domain.blog.dto;

import com.ndgl.spotfinder.domain.post.entity.Post;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostSummeryDto(
	@Schema(description = "ID", example = "1")
	Long id,

	@Schema(description = "제목", example = "맛있는 녀석들에 나온 맛집들")
	String title
) {
	public PostSummeryDto(Post post) {
		this(post.getId(), post.getTitle());
	}
}
