package com.ndgl.spotfinder.domain.like.entity;

import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Likes extends BaseTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "target_id", nullable = false)
	private Long targetId;

	@Column(name = "target_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private TargetType targetType;

	public enum TargetType {
		POST,      // 포스트
		COMMENT,   // 댓글
	}

	public static Likes createPostLike(Long userId, Long postId) {
		return builder()
			.userId(userId)
			.targetId(postId)
			.targetType(TargetType.POST)
			.build();
	}

	public static Likes createCommentLike(Long userId, Long commentId) {
		return builder()
			.userId(userId)
			.targetId(commentId)
			.targetType(TargetType.COMMENT)
			.build();
	}

	public boolean isPostLike() {
		return TargetType.POST.equals(targetType);
	}

	public boolean isCommentLike() {
		return TargetType.COMMENT.equals(targetType);
	}

}
