package com.ndgl.spotfinder.domain.image.dto;

import java.util.List;

/**
 * 이미지 업로드 완료 요청 DTO
 */
public record UploadCompleteRequest(
	List<String> imageUrls
) {
}