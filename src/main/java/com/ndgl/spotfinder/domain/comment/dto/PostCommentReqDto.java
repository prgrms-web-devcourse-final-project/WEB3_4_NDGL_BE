package com.ndgl.spotfinder.domain.comment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PostCommentReqDto {
	@NotNull @Size(min = 2)
	private String content;
}
