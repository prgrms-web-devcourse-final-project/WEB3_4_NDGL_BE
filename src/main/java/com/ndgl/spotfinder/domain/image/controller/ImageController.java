package com.ndgl.spotfinder.domain.image.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.image.dto.ImageUrlRequestDto;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponseDto;
import com.ndgl.spotfinder.domain.image.dto.UploadCompleteRequestDto;
import com.ndgl.spotfinder.domain.image.service.ImageService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController implements ImageApiSpecification {

	private final ImageService imageService;

	@PostMapping("/presigned-url")
	public RsData<PresignedUrlsResponseDto> createPresignedUrl(
		@Valid @RequestBody ImageUrlRequestDto rq
	) {
		PresignedUrlsResponseDto rs = imageService.createImage(rq);
		return RsData.success(HttpStatus.OK, rs);
	}

	@PostMapping("/upload-complete")
	public RsData<String> uploadComplete(
		@Valid @RequestBody UploadCompleteRequestDto rq
	) {
		imageService.saveImages(rq);
		return RsData.success(HttpStatus.OK);
	}

}
