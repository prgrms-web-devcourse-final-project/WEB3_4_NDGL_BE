package com.ndgl.spotfinder.domain.comment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostCommentReqDto(
	@NotNull @Size(min = 2, max = 100)
	String content,

	Long parentId
) { }
