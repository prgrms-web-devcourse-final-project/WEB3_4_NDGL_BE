package com.ndgl.spotfinder.domain.image.dto;

import java.util.List;

import com.ndgl.spotfinder.domain.image.type.ImageType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "이미지 업로드 완료 DTO")
public record UploadCompleteRequest(
	@Schema(description = "이미지 참조 객체 ID", example = "1")
	@NotNull(message = "ID는 필수입니다.")
	@Min(value = 1, message = "ID는 최소 1 이상입니다.")
	long id,

	@Schema(description = "이미지 참조 타입", example = "POST")
	@NotNull(message = "imageType은 필수입니다.")
	ImageType imageType,

	@Schema(
		description = "업로드된 이미지 URL 목록",
		example = "[\"https://s3-url-example.com/image1.jpg\", \"https://s3-url-example.com/image2.jpg\"]")
	@NotNull(message = "URL은 필수입니다.")
	@Size(min = 1, message = "URL이 최소 1개 이상 입력되어야 합니다.")
	@Valid
	List<String> imageUrl
) {
}