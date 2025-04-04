package com.ndgl.spotfinder.domain.comment.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.base.BaseTime;
import com.ndgl.spotfinder.global.exception.ErrorCode;

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

	@ManyToOne(fetch = FetchType.LAZY)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	// 좋아요 수
	@Column(nullable = false)
	private Long likeCount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private PostComment parentComment;

	@OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostComment> childrenComments = new ArrayList<>();

	public void isCommentOfPost(Long postId) {
		if (!this.post.getId().equals(postId)) {
			ErrorCode.NOT_FOUND_IN_POST.throwServiceException();
		}
	}

	public void checkAuthorCanModify(User author) {
		if (!this.user.equals(author)) {
			ErrorCode.UNAUTHORIZED.throwServiceException();
		}
	}

	public void checkAuthorCanDelete(User author) {
		if (!this.user.equals(author)) {
			ErrorCode.UNAUTHORIZED.throwServiceException();
		}
	}

	public void updateLikeCount(long num) {
		this.likeCount += num;
	}
}
