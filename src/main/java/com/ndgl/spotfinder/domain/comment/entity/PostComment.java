package com.ndgl.spotfinder.domain.comment.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.http.HttpStatus;

import com.ndgl.spotfinder.global.base.BaseTime;
import com.ndgl.spotfinder.global.exception.ServiceException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

	// 작성자(임시)
	@Column(nullable = false)
	private Long userId;

	// 좋아요 수
	@Column(nullable = false)
	private Long likeCount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private PostComment parentComment;

	@OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostComment> childrenComments = new ArrayList<>();

	public void isCommentOfPost(Long postId) {
		if (!this.postId.equals(postId)) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "Not Found In Post");
		}
	}

	public boolean hasParent() {
		return parentComment != null;
	}

	public boolean hasChildren() {
		return !childrenComments.isEmpty();
	}
}
