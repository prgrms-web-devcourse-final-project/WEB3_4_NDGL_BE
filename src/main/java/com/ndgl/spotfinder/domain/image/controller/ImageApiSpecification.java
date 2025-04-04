package com.ndgl.spotfinder.domain.image.controller;

import org.springframework.web.bind.annotation.RequestBody;

import com.ndgl.spotfinder.domain.image.dto.ImageRequest;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponse;
import com.ndgl.spotfinder.domain.image.dto.UploadCompleteRequest;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

public interface ImageApiSpecification {
	@Operation(
		summary = "Presigned URL 생성",
		description = "S3 이미지 업로드를 위한 Presigned URL을 생성합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Presigned URL 생성 성공",
				content = @Content(schema = @Schema(implementation = PresignedUrlsResponse.class))
			)
		},
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<PresignedUrlsResponse> createPresignedUrl(
		@Valid @RequestBody ImageRequest rq
	);

	@Operation(
		summary = "이미지 업로드 완료 처리",
		description = "S3에 이미지 업로드 완료 후 DB에 이미지 정보를 저장합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "업로드 완료 처리 성공"
			)
		},
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<String> uploadComplete(
		@Valid @RequestBody UploadCompleteRequest rq
	);

}
