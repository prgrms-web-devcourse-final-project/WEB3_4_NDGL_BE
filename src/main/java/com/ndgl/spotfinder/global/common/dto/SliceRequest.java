package com.ndgl.spotfinder.global.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SliceRequest(
	@Schema(description = "마지막 항목의 ID", example = "24")
	@PositiveOrZero(message = "요청하는 lastId는 0 이상이어야 합니다.")
	Long lastId,

	@Schema(description = "한 번에 요청할 데이터 개수", example = "5")
	@NotNull
	@Positive(message = "요청 Slice 사이즈는 양수여야 합니다.")
	Integer size
) {
}
