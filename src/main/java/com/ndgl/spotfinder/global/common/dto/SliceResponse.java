package com.ndgl.spotfinder.global.common.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record SliceResponse<T>(
	@Schema(description = "요청한 데이터")
	List<T> contents,

	@Schema(description = "다음 데이터 유무")
	boolean hasNext
) {
}
