package com.ndgl.spotfinder.domain.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.LastModifiedDate;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.post.dto.HashtagDto;
import com.ndgl.spotfinder.domain.post.dto.LocationDto;
import com.ndgl.spotfinder.domain.post.dto.PostCommonUpdateRequestDto;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Post extends BaseTime {
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
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Hashtag> hashtags = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Location> locations = new ArrayList<>();

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private PostStatus status = PostStatus.TEMP;

	public static Post createTempPost(User user) {
		return Post.builder()
			.title("")
			.content("")
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

	public Post updatePost(PostCommonUpdateRequestDto requestDto, boolean temp) {
		title = requestDto.title();
		content = requestDto.content();
		thumbnail = requestDto.thumbnail();
		this.status = temp ? PostStatus.TEMP : PostStatus.PUBLIC;

		List<Hashtag> newHashtags = requestDto.hashtags()
			.stream()
			.map(HashtagDto::toHashtag)
			.toList();
		updateHashtags(newHashtags);

		List<Location> newLocations = requestDto.locations()
			.stream()
			.map(LocationDto::toLocation)
			.toList();
		updateLocations(newLocations);

		return this;
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
}
