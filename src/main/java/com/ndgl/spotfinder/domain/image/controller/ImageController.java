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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

	private final ImageService imageService;

	@PostMapping("/presigned-url")
	public RsData<PresignedUrlsResponse> createPresignedUrl(@Valid @RequestBody ImageRequest rq) {
		log.info("Presigned URL 요청 받음: referenceId={}, type={}, extensions={}",
			rq.referenceId(), rq.imageType(), rq.imageExtensions());

		PresignedUrlsResponse rs = imageService.createImage(rq);
		return RsData.success(HttpStatus.OK, rs);
	}

	@PostMapping("/complete")
	public RsData<String> uploadComplete(@Valid @RequestBody UploadCompleteRequest rq) {
		log.info("업로드 완료 요청 받음: referenceId={}, type={}, urls 개수={}",
			rq.id(), rq.imageType(), rq.imageUrl().size());

		imageService.saveImages(rq);
		return RsData.success(HttpStatus.OK);
	}

	@DeleteMapping
	public RsData<String> deleteImage(@RequestParam String url) {
		log.info("삭제 URL : url={}", url);
		imageService.deleteImageByUrl(url);
		return RsData.success(HttpStatus.OK, "이미지가 성공적으로 삭제되었습니다.");
	}
}
