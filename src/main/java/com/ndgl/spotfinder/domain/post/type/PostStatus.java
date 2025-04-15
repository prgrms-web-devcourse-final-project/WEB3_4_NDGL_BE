package com.ndgl.spotfinder.domain.post.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {
	PUBLIC("공개"),
	TEMP("임시 글"),
	BLIND("블라인드");

	private final String value;
}
