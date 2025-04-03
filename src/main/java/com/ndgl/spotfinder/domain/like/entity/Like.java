package com.ndgl.spotfinder.domain.like.entity;

import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "target_id", nullable = false)
	private Long targetId;

	@Column(name = "target_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private TargetType targetType;

	public enum TargetType {
		POST,      // 포스트
		COMMENT,   // 댓글
	}

}
