package com.ndgl.spotfinder.domain.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.LastModifiedDate;

import com.ndgl.spotfinder.domain.post.dto.HashtagDto;
import com.ndgl.spotfinder.domain.post.dto.LocationDto;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Post extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String content;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	private String thumbnail;

	@Builder.Default
	private Long viewCount = 0L;

	@Builder.Default
	private Long likeCount = 0L;

	@Builder.Default
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Hashtag> hashtags = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Location> locations = new ArrayList<>();

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

	public Post updatePost(PostUpdateRequestDto requestDto) {
		title = requestDto.title();
		content = requestDto.content();
		thumbnail = requestDto.thumbnail();

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
}
