package com.ndgl.spotfinder.domain.like.entity;

import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "`Like`",
	uniqueConstraints = @UniqueConstraint(
		columnNames = {"user_id", "target_id", "target_type"},
		name = "uk_like_user_target"
	))
public class Like extends BaseTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// TODO : 유저 관계 추후 추가
	@Column(name = "user_id", nullable = false)
	private Long userId;

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "user_id", nullable = false)
	// private User user;

	@Column(name = "target_id", nullable = false)
	private Long targetId;

	@Column(name = "target_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private TargetType targetType;

	public enum TargetType {
		POST,      // 포스트
		COMMENT,   // 댓글
	}

	public static Like createPostLike(Long userId, Long postId) {
		return builder()
			.userId(userId)
			.targetId(postId)
			.targetType(TargetType.POST)
			.build();
	}

	public static Like createCommentLike(Long userId, Long commentId) {
		return builder()
			.userId(userId)
			.targetId(commentId)
			.targetType(TargetType.COMMENT)
			.build();
	}

}
