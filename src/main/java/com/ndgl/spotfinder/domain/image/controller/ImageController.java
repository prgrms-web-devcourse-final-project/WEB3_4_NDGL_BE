package com.ndgl.spotfinder.domain.image.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.image.dto.ImageRequest;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponse;
import com.ndgl.spotfinder.domain.image.dto.UploadCompleteRequest;
import com.ndgl.spotfinder.domain.image.service.ImageService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "이미지 API", description = "이미지 관련 API")
public class ImageController {

	private final ImageService imageService;

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
	@PostMapping("/presigned-url")
	public RsData<PresignedUrlsResponse> createPresignedUrl(
		@Valid @RequestBody ImageRequest rq
	) {
		log.info("Presigned URL 요청 받음: referenceId={}, type={}, extensions={}",
			rq.referenceId(), rq.imageType(), rq.imageExtensions());

		PresignedUrlsResponse rs = imageService.createImage(rq);
		return RsData.success(HttpStatus.OK, rs);
	}

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
	@PostMapping("/complete")
	public RsData<String> uploadComplete(
		@Valid @RequestBody UploadCompleteRequest rq
	) {
		log.info("업로드 완료 요청 받음: referenceId={}, type={}, urls 개수={}",
			rq.id(), rq.imageType(), rq.imageUrl().size());

		imageService.saveImages(rq);
		return RsData.success(HttpStatus.OK);
	}

	/**
	 * 미 확인 상태
	 */
	@Deprecated
	@DeleteMapping
	public RsData<String> deleteImage(@RequestParam String url) {
		log.info("삭제 URL : url={}", url);
		imageService.deleteImageByUrl(url);
		return RsData.success(HttpStatus.OK, "이미지가 성공적으로 삭제되었습니다.");
	}
}
