package com.ndgl.spotfinder.domain.post.dto;

import com.ndgl.spotfinder.domain.post.entity.Hashtag;

public record HashtagDto(
	String name
) {
	public Hashtag toHashtag() {
		return Hashtag.builder()
			.name(name)
			.build();
	}
}
