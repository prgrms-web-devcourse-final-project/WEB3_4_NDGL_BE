package com.ndgl.spotfinder.global.common.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SliceRequest(
	@PositiveOrZero(message = "요청하는 lastId는 0 이상이어야 합니다.")
	Long lastId,

	@Positive(message = "요청 Slice 사이즈는 양수여야 합니다.")
	Integer size
) {
}
