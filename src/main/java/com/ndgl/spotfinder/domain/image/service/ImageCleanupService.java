package com.ndgl.spotfinder.domain.image.service;

import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.global.aws.s3.S3Service;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageCleanupService {

	private final ImageRepository imageRepository;
	private final S3Service s3Service;

	/**
	 * 포스트 내용에서 사용되지 않는 이미지를 비동기적으로 삭제
	 *
	 * @param imageType     이미지 타입 (POST 등)
	 * @param referenceId   참조 ID (포스트 ID 등)
	 * @param usedImageUrls 컨텐츠에서 실제 사용 중인 이미지 URL 목록
	 */
	@Async("imageCleanupExecutor")
	@Transactional
	public void cleanupUnusedImages(ImageType imageType, long referenceId, Set<String> usedImageUrls) {
		// DB에서 해당 객체와 연결된 모든 이미지 가져오기
		// TODO 임시 하드코딩
		List<Image> savedImages = imageRepository.findByImageTypeAndReferenceId(imageType, 999L);

		// 실제 사용되지 않는 이미지 필터링
		List<Image> unusedImages = savedImages.stream()
			.filter(image -> !usedImageUrls.contains(image.getUrl()))
			.toList();

		// 미사용 이미지 삭제 (S3 및 DB에서)
		for (Image image : unusedImages) {
			try {
				s3Service.deleteFile(image.getUrl());
				imageRepository.delete(image);
			} catch (Exception e) {
				throw ErrorCode.IMAGE_DELETE_ERROR.throwServiceException();
			}
		}

	}
} 