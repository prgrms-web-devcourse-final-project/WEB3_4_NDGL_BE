package com.ndgl.spotfinder.domain.blog.dto;

import java.util.List;

public record BlogResponseDto(
	Long id,
	String blogName,
	String nickname,
	Boolean isFollowed,
	List<PostSummeryDto> posts
) {
}
