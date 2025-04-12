package com.ndgl.spotfinder.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminCreateResponseDto(
	@Schema(description = "생성된 관리자 ID", example = "1")
	long id
) {
}
