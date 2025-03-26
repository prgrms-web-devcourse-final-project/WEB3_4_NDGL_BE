package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Location;
import com.ndgl.spotfinder.domain.post.entity.Post;

public record PostCreateRequestDto(
	String title,
	String content,
	List<HashtagDto> hashtags,
	List<LocationDto> locations
) {
	public Post toPost() {
		List<Location> locationEntities = this.locations
			.stream()
			.map(LocationDto::toLocation)
			.toList();

		List<Hashtag> hashtagEntities = this.hashtags
			.stream()
			.map(HashtagDto::toHashtag)
			.toList();

		Post post = Post.builder()
			.title(title)
			.content(content)
			.viewCount(0L)
			.likeCount(0L)
			.build();

		locationEntities.forEach(post::addLocation);
		hashtagEntities.forEach(post::addHashtag);

		return post;
	}
}
