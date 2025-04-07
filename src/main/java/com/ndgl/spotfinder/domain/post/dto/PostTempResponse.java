package com.ndgl.spotfinder.domain.post.dto;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.entity.PostStatus;

public record PostTempResponse(
	Long id,
	String title,
	String content,
	PostStatus status
) {

	public static PostTempResponse from(Post post) {
		return new PostTempResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getStatus()
		);
	}

}