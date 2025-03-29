package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Location;
import com.ndgl.spotfinder.domain.post.entity.Post;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostCreateRequestDto(
	@NotBlank
	@Size(max = 100)
	String title,

	@NotBlank
	@Size(max = 16000)
	String content,

	@NotNull
	@Size(min = 1, max = 10)
	@Valid
	List<HashtagDto> hashtags,

	@NotNull
	@Size(min = 1, max = 20)
	@Valid
	List<LocationDto> locations
) {
	public Post toPost() {
		List<Hashtag> hashtagEntities = hashtags
			.stream()
			.map(HashtagDto::toHashtag)
			.toList();

		List<Location> locationEntities = locations
			.stream()
			.map(LocationDto::toLocation)
			.toList();

		Post post = Post.builder()
			.title(title)
			.content(content)
			.viewCount(0L)
			.likeCount(0L)
			.build();

		post.addHashtags(hashtagEntities);
		post.addLocations(locationEntities);

		return post;
	}
}
