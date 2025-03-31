package com.ndgl.spotfinder.domain.post.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.post.entity.Post;

public record PostResponseDto(
	Long id,
	String title,
	String content,
	String authorName,
	String thumbnail,
	Long likeCount,
	Long commentCount,
	LocalDateTime createdAt
) {
	public PostResponseDto(Post post) {
		this(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getUser().getNickName(),
			post.getThumbnail(),
			post.getLikeCount(),
			0L,
			post.getCreatedAt()
		);
	}
}
