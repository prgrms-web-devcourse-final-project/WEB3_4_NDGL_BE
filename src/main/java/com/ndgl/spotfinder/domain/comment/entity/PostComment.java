package com.ndgl.spotfinder.domain.comment.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.http.HttpStatus;

import com.ndgl.spotfinder.global.base.BaseTime;
import com.ndgl.spotfinder.global.exception.ServiceException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PostComment extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TEXT", nullable = false)
	@Setter
	private String content;

	@LastModifiedDate
	private LocalDateTime modifiedAt;

	// 포스트 -> id로 대체(임시)
	@Column(nullable = false)
	private Long postId;

	// 작성자
	@Column(nullable = false)
	private Long userId;

	// 부모 댓글 id(nullable)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private PostComment parentComment;

	public void isCommentOfPost(Long postId) {
		if (!this.postId.equals(postId)) {
			throw new ServiceException(HttpStatus.NOT_FOUND, "Not Found In Post");
		}
	}
}
