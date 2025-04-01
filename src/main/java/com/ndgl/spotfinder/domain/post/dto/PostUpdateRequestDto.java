package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpdateRequestDto(
	@NotBlank(message = "제목은 1자 이상입니다.")
	@Size(max = 100, message = "제목은 100자 이하입니다.")
	String title,

	@NotBlank(message = "내용은 1자 이상입니다.")
	@Size(max = 16000, message = "내용은 16000자 이하입니다.")
	String content,

	@NotNull(message = "해시태그는 필수입니다.")
	@Size(min = 1, max = 10, message = "해시태그는 1 ~ 10개 입니다.")
	@Valid
	List<HashtagDto> hashtags,

	@NotNull(message = "장소는 필수입니다.")
	@Size(min = 1, max = 20, message = "장소는 1 ~ 20개 입니다.")
	@Valid
	List<LocationDto> locations,

	@NotNull(message = "썸네일 이미지는 필수입니다.")
	String thumbnail
) {
}
