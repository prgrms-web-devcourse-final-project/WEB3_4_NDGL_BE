package com.ndgl.spotfinder.domain.comment.dto;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;

import lombok.Getter;

@Getter
public class PostCommentDto {
	private final Long id;
	private final String content;
	// private final String authorName;
	// private final Long postId;
	private final String createdAt;
	private final String modifiedAt;

	public PostCommentDto(PostComment comment) {
		this.id = comment.getId();
		this.content = comment.getContent();
		// this.authorName = comment.getAuthor().getName();
		// this.postId = comment.getPost().getId();
		this.createdAt = comment.getCreatedAt().toString();
		this.modifiedAt = comment.getModifiedAt().toString();
	}
}
