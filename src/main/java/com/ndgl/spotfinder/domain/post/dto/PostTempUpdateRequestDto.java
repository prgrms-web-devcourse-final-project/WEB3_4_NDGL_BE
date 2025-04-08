package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record PostTempUpdateRequestDto (

	@Size(max = 100, message = "제목은 100자 이하입니다.")
	String title,

	@Size(max = 16000, message = "내용은 16000자 이하입니다.")
	String content,

	@Size(min = 1, max = 10, message = "해시태그는 1 ~ 10개 입니다.")
	@Valid
	List<HashtagDto> hashtags,

	@Size(min = 1, max = 20, message = "장소는 1 ~ 20개 입니다.")
	@Valid
	List<LocationDto> locations,

	String thumbnail
) implements PostCommonUpdateRequestDto {
}
