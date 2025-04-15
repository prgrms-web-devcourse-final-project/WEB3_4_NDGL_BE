package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.type.PostStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostTempResponseDto(
	@Schema(description = "ID", example = "1")
	Long id,

	@Schema(description = "제목", example = "")
	String title,

	@Schema(description = "내용", example = "")
	String content,

	@Schema(description = "썸네일 이미지 URL", example = "")
	String thumbnail,

	@Schema(description = "해시태그 목록", example = "[]")
	List<HashtagDto> hashtags,

	@Schema(description = "장소 목록", example = "[]")
	List<LocationDto> locations,

	@Schema(description = "포스트 상태", example = "TEMP(임시글)")
	PostStatus status
) {

	public static PostTempResponseDto from(Post post) {
		return new PostTempResponseDto(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getThumbnail(),
			post.getHashtags()
				.stream()
				.map(HashtagDto::new)
				.toList(),
			post.getLocations()
				.stream()
				.map(LocationDto::new)
				.toList(),
			post.getStatus()
		);
	}

}
