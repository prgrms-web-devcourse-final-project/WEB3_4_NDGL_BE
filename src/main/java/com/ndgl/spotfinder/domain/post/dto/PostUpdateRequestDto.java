package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpdateRequestDto(
	@NotBlank
	@Size(max = 100)
	String title,

	@NotBlank
	@Size(max = 16000)
	String content,

	@NotNull
	@Size(max = 10)
	@Valid
	List<HashtagDto> hashtags,

	@NotNull
	@Size(min = 1, max = 20)
	@Valid
	List<LocationDto> locations
) {
}
