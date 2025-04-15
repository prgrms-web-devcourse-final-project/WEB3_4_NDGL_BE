package com.ndgl.spotfinder.domain.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.LastModifiedDate;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.like.entity.Likeable;
import com.ndgl.spotfinder.domain.post.type.PostStatus;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "post", indexes = {
	@Index(name = "idx_post_created_at", columnList = "created_at DESC")
})
public class Post extends BaseTime implements Likeable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Column(length = 100)
	private String title;

	@Setter
	@Column(columnDefinition = "TEXT")
	private String content;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Setter
	private String thumbnail;

	@Builder.Default
	private Long viewCount = 0L;

	@Builder.Default
	private Long likeCount = 0L;

	@Builder.Default
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PostComment> comments = new ArrayList<>();

	@Builder.Default
	@BatchSize(size = 50)
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Hashtag> hashtags = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Location> locations = new ArrayList<>();

	@Setter
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private PostStatus status = PostStatus.TEMP;

	public static Post createTempPost(User user) {
		return Post.builder()
			.title("")
			.content("")
			.thumbnail("")
			.status(PostStatus.TEMP)
			.user(user)
			.build();
	}

	public void addHashtag(Hashtag hashtag) {
		hashtags.add(hashtag);
		hashtag.setPost(this);
	}

	public void addHashtags(List<Hashtag> hashtags) {
		hashtags.forEach(this::addHashtag);
	}

	public void addLocation(Location location) {
		locations.add(location);
		location.setPost(this);
	}

	public void addLocations(List<Location> locations) {
		locations.forEach(this::addLocation);
	}

	public void updateHashtags(List<Hashtag> newHashtags) {
		removeAllHashtags();
		addHashtags(newHashtags);
	}

	public void updateLocations(List<Location> newLocations) {
		removeAllLocations();
		addLocations(newLocations);
	}

	public void removeAllHashtags() {
		hashtags.clear();
	}

	public void removeAllLocations() {
		locations.clear();
	}

	public void updateLikeCount(long num) {
		this.likeCount += num;
	}

	public void changeStatus(PostStatus newStatus) {
		if (this.status == PostStatus.TEMP && newStatus == PostStatus.PUBLIC)
			this.createdAt = LocalDateTime.now(); // 발행일으로 변경시, 생성일 업데이트
		this.status = newStatus;
	}

	@Override
	public void removeLike() {
		this.likeCount--;
	}

	@Override
	public void addLike() {
		this.likeCount++;
	}
}
