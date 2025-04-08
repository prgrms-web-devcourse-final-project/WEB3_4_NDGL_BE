package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record PostTempUpdateRequestDto(

	@Schema(description = "제목 (100자 이하)", example = "맛집 탐방기")
	@Size(max = 100, message = "제목은 100자 이하입니다.")
	String title,

	@Schema(description = "내용 (16000자 이하)", example = "오늘 방문한 맛집에 대한 후기입니다.")
	@Size(max = 16000, message = "내용은 16000자 이하입니다.")
	String content,

	@Size(min = 1, max = 10, message = "해시태그는 1 ~ 10개 입니다.")
	@Valid
	List<HashtagDto> hashtags,

	@Size(min = 1, max = 20, message = "장소는 1 ~ 20개 입니다.")
	@Valid
	List<LocationDto> locations,

	@Schema(description = "썸네일 이미지 URL", example = "http://example.com/image.jpg")
	String thumbnail
) implements PostCommonUpdateRequestDto {
}
