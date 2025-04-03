package com.ndgl.spotfinder.domain.image.dto;

import java.util.List;

import com.ndgl.spotfinder.domain.image.type.ImageType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "이미지 업로드 요청 DTO")
public record ImageRequest(
	@Schema(description = "이미지 참조 객체 ID", example = "1")
	@NotNull(message = "ID는 필수입니다.")
	@Min(value = 1, message = "ID는 최소 1 이상입니다.")
	long referenceId,

	@Schema(description = "이미지 참조 타입", example = "POST")
	@NotNull(message = "imageType은 필수입니다.")
	ImageType imageType,

	@Schema(description = "허용 가능한 확장자 목록", example = "[\"png\", \"jpg\"]")
	@NotNull(message = "확장자는 필수입니다.")
	@Size(min = 1, message = "URL이 최소 1개 이상 입력되어야 합니다.")
	@Valid
	List<String> imageExtensions
) {
}
