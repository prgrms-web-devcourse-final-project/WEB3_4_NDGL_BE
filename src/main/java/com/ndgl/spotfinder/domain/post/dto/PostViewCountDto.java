package com.ndgl.spotfinder.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostViewCountDto {
	private Long postId;
	private Long viewCount;
}
