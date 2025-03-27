package com.ndgl.spotfinder.domain.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.LastModifiedDate;

import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	public void addLocation(Location location) {
		locations.add(location);
		location.setPost(this);
	}

	public void addHashtag(Hashtag hashtag) {
		hashtags.add(hashtag);
		hashtag.setPost(this);
	}
}
