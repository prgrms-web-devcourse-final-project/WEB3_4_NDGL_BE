package com.ndgl.spotfinder.domain.image.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.image.dto.ImageRequest;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponse;
import com.ndgl.spotfinder.domain.image.dto.UploadCompleteRequest;
import com.ndgl.spotfinder.domain.image.service.ImageService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "이미지 API", description = "이미지 관련 API")
public class ImageController implements ImageApiSpecification {

	private final ImageService imageService;

	@PostMapping("/presigned-url")
	public RsData<PresignedUrlsResponse> createPresignedUrl(
		@Valid @RequestBody ImageRequest rq
	) {
		PresignedUrlsResponse rs = imageService.createImage(rq);
		return RsData.success(HttpStatus.OK, rs);
	}

	@PostMapping("/complete")
	public RsData<String> uploadComplete(
		@Valid @RequestBody UploadCompleteRequest rq
	) {
		imageService.saveImages(rq);
		return RsData.success(HttpStatus.OK);
	}

}
