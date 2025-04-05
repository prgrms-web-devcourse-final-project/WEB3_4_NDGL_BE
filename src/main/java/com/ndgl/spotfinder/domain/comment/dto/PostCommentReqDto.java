package com.ndgl.spotfinder.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCommentReqDto(
	@NotBlank(message = "댓글 내용은 필수입니다.")
	@Size(min = 2, max = 100, message = "댓글 내용은 2자 이상 100자 이하입니다.")
	String content,

	Long parentId
) { }
