package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.entity.PostStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostTempResponse(
	@Schema(description = "ID", example = "1")
	Long id,

	@Schema(description = "제목", example = "맛있는 녀석들에 나온 맛집들")
	String title,

	@Schema(description = "내용", example = "TV 예능 맛있는 녀석들에 나온 맛집들입니다.")
	String content,

	@Schema(description = "썸네일 이미지 URL", example = "https://image.com/images/thumbnail1.jpg")
	String thumbnail,

	@Schema(description = "해시태그 목록")
	List<HashtagDto> hashtags,

	@Schema(description = "장소 목록")
	List<LocationDto> locations,

	@Schema(description = "포스트 상태", example = "TEMP(임시글), PUBLIC(공개), BLIND(블라인드)")
	PostStatus status
) {

	public static PostTempResponse from(Post post) {
		return new PostTempResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getThumbnail(),
			post.getHashtags()
				.stream()
				.limit(3)
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