package com.ndgl.spotfinder.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;

import lombok.Getter;

@Getter
public class PostCommentDto {
	private final Long id;
	private final String content;
	private final String authorName;
	private final Long postId;
	private final Long parentId;
	private final Long likeCount;
	private final LocalDateTime createdAt;
	private final LocalDateTime modifiedAt;
	private final List<PostCommentDto> replies;

	public PostCommentDto(PostComment comment) {
		this.id = comment.getId();
		this.content = comment.getContent();
		this.authorName = comment.getUser().getNickName();
		this.postId = comment.getPost().getId();
		this.parentId = (comment.getParentComment() != null) ? comment.getParentComment().getId() : null;
		this.likeCount = comment.getLikeCount();
		this.createdAt = comment.getCreatedAt();
		this.modifiedAt = comment.getModifiedAt();
		this.replies = comment.getChildrenComments() == null ? new ArrayList<>()
			: comment.getChildrenComments().stream()
			.sorted(Comparator.comparing(PostComment::getId).reversed())
			.map(PostCommentDto::new)
			.toList();
	}
}
