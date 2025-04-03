package com.ndgl.spotfinder.domain.image.dto;

import java.util.List;

import com.ndgl.spotfinder.domain.image.type.ImageType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UploadCompleteRequest(
	@NotNull(message = "ID는 필수입니다.")
	@Min(value = 1, message = "ID는 최소 1 이상입니다.")
	long id,

	@NotNull(message = "imageType은 필수입니다.")
	ImageType imageType,

	@NotNull(message = "URL은 필수입니다.")
	@Size(min = 1, message = "URL이 최소 1개 이상 입력되어야 합니다.")
	@Valid
	List<String> imageUrl
) {
}