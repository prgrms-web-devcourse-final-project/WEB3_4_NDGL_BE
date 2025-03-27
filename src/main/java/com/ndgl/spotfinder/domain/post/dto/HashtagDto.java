package com.ndgl.spotfinder.domain.post.dto;

import com.ndgl.spotfinder.domain.post.entity.Hashtag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HashtagDto(
	@NotBlank
	@Size(max = 30)
	String name
) {
	public Hashtag toHashtag() {
		return Hashtag.builder()
			.name(name)
			.build();
	}
}
