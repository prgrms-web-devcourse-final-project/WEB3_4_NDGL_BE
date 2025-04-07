package com.ndgl.spotfinder.domain.post.dto;

import com.ndgl.spotfinder.domain.post.entity.Post;

public record PostTempResponse(
	Long id,
	String title,
	String content
) {

	public static PostTempResponse from(Post post) {
		return new PostTempResponse(
			post.getId(),
			post.getTitle(),
			post.getContent()
		);
	}


	public PostTempResponse(Long id) {
		this(id, null, null);
	}
}