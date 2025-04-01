package com.ndgl.spotfinder.domain.post.dto;

import com.ndgl.spotfinder.domain.post.entity.Hashtag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HashtagDto(
	@NotBlank(message = "해시태그 이름은 필수입니다.")
	@Size(max = 30, message = "해시태그는 30자 이하입니다.")
	String name
) {
	public Hashtag toHashtag() {
		return Hashtag.builder()
			.name(name)
			.build();
	}
}
