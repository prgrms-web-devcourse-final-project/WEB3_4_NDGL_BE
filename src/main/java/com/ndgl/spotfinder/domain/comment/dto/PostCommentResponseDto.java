package com.ndgl.spotfinder.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCommentResponseDto(
	@Schema(description = "ID", example = "1")
	Long id,

	@Schema(description = "댓글 내용", example = "이 포스트 정말 유용하네요!")
	String content,

	@Schema(description = "작성자 아이디", example = "2")
	Long authorId,

	@Schema(description = "작성자 이름", example = "홍길동")
	String authorName,

	@Schema(description = "게시물 ID", example = "1")
	Long postId,

	@Schema(description = "부모 댓글 ID", example = "1")
	Long parentId,

	@Schema(description = "좋아요 수", example = "50")
	Long likeCount,

	@Schema(description = "작성 일자", example = "2025-03-23T14:30:00")
	LocalDateTime createdAt,

	@Schema(description = "수정 일자", example = "2025-03-23T14:30:00")
	LocalDateTime modifiedAt,

	@Schema(description = "대댓글 목록")
	List<PostCommentResponseDto> replies,

	@Schema(description = "좋아요 여부", example = "false")
	Boolean likeStatus
) {
	public PostCommentResponseDto(PostComment comment, Boolean isLiked) {
		this(
			comment.getId(),
			comment.getContent(),
			comment.getUser().getId(),
			comment.getUser().getNickName(),
			comment.getPost().getId(),
			(comment.getParentComment() != null) ? comment.getParentComment().getId() : null,
			comment.getLikeCount(),
			comment.getCreatedAt(),
			comment.getModifiedAt(),
			comment.getChildrenComments() == null ? new ArrayList<>()
				: comment.getChildrenComments().stream()
				.sorted(Comparator.comparing(PostComment::getId).reversed())
				.map(PostCommentResponseDto::new)
				.toList(),
			isLiked
		);
	}

	public PostCommentResponseDto(PostComment comment) {
		this(
			comment.getId(),
			comment.getContent(),
			comment.getUser().getId(),
			comment.getUser().getNickName(),
			comment.getPost().getId(),
			(comment.getParentComment() != null) ? comment.getParentComment().getId() : null,
			comment.getLikeCount(),
			comment.getCreatedAt(),
			comment.getModifiedAt(),
			comment.getChildrenComments() == null ? new ArrayList<>()
				: comment.getChildrenComments().stream()
				.sorted(Comparator.comparing(PostComment::getId).reversed())
				.map(PostCommentResponseDto::new)
				.toList(),
			false
		);
	}
}
