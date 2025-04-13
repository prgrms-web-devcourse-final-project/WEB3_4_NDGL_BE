package com.ndgl.spotfinder.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.entity.PostCommentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCommentResponseDto(
	@Schema(description = "ID", example = "1")
	Long id,

	@Schema(description = "댓글 내용", example = "이 포스트 정말 유용하네요!")
	String content,

	@Schema(description = "작성자 ID", example = "2")
	Long authorId,

	@Schema(description = "작성자 이름", example = "홍길동")
	String authorName,

	@Schema(description = "게시물 ID", example = "1")
	Long postId,

	@Schema(description = "부모 댓글 ID", example = "1")
	Long parentId,

	@Schema(description = "좋아요 수", example = "50")
	Long likeCount,

	@Schema(description = "고정 여부", example = "false")
	Boolean pinned,

	@Schema(description = "댓글 상태", example = "PUBLIC")
	PostCommentStatus status,

	@Schema(description = "작성 일자", example = "2025-03-23T14:30:00")
	LocalDateTime createdAt,

	@Schema(description = "수정 일자", example = "2025-03-23T14:30:00")
	LocalDateTime modifiedAt,

	@Schema(description = "대댓글 목록")
	List<PostCommentResponseDto> replies,

	@Schema(description = "좋아요 여부", example = "false")
	Boolean likeStatus
) {
	public PostCommentResponseDto(PostComment comment, Boolean isLiked, List<PostCommentResponseDto> childrenComments) {
		this(
			comment.getId(),
			isPublic(comment) ? comment.getContent() : null,
			isPublic(comment) ? comment.getUser().getId() : null,
			isPublic(comment) ? comment.getUser().getNickName() : null,
			comment.getPost().getId(),
			comment.getParentComment() != null ? comment.getParentComment().getId() : null,
			isPublic(comment) ? comment.getLikeCount() : null,
			isPublic(comment) ? comment.isPinned() : null,
			comment.getStatus(),
			comment.getCreatedAt(),
			comment.getModifiedAt(),
			childrenComments,
			isLiked
		);
	}

	public PostCommentResponseDto(PostComment comment, Boolean isLiked) {
		this(
			comment,
			isLiked,
			comment.getChildrenComments() == null ? Collections.emptyList()
				: comment.getChildrenComments().stream()
				.sorted(Comparator.comparing(PostComment::getId).reversed())
				.map(child -> new PostCommentResponseDto(child, false))
				.toList()
		);
	}

	public PostCommentResponseDto(PostComment comment) {
		this(comment, false);
	}

	private static boolean isPublic(PostComment comment) {
		return comment.getStatus() == PostCommentStatus.PUBLIC;
	}
}
