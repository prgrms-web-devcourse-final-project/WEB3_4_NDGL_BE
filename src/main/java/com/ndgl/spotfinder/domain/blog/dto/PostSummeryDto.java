package com.ndgl.spotfinder.domain.blog.dto;

import com.ndgl.spotfinder.domain.post.entity.Post;

public record PostSummeryDto(
	Long id,
	String title
) {
	public PostSummeryDto(Post post) {
		this(post.getId(), post.getTitle());
	}
}
