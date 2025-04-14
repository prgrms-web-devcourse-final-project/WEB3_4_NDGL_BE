package com.ndgl.spotfinder.domain.comment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCommentStatus {
	PUBLIC("공개"),
	DELETED("삭제"),
	BLINDED("블라인드");

	private final String value;
}
