package com.ndgl.spotfinder.domain.image.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageUsage;
import com.ndgl.spotfinder.global.aws.s3.S3Service;

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
	 * @param imageUsage       이미지 타입 (POST 등)
	 * @param referenceId      참조 ID (포스트 ID 등)
	 * @param usedImageUrls    컨텐츠에서 실제 사용 중인 이미지 URL 목록
	 * @param currentThumbnail 현재 포스트의 썸네일 URL
	 */
	@Async("imageCleanupExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void cleanupUnusedImages(
		ImageUsage imageUsage,
		long referenceId,
		Set<String> usedImageUrls,
		String currentThumbnail
	) {
		Set<String> allUsedImages = new HashSet<>(usedImageUrls);
		if (StringUtils.hasText(currentThumbnail)) {
			allUsedImages.add(currentThumbnail);
		}

		try {
			List<Image> savedImages = imageRepository.findByImageUsageAndReferenceId(imageUsage, referenceId);

			savedImages.stream()
				.filter(image -> !allUsedImages.contains(image.getUrl()))
				.forEach(image -> {
					try {
						s3Service.deleteFile(image.getUrl());
						imageRepository.delete(image);
					} catch (Exception e) {
						log.error("이미지 삭제 중 오류 발생: {}", image.getUrl(), e);
					}
				});

			log.debug("이미지 정리 완료: imageUsage={}, referenceId={}, 이미지 수: {}",
				imageUsage, referenceId, savedImages.size());
		} catch (Exception e) {
			log.error("이미지 정리 중 예외 발생: imageUsage={}, referenceId={}", imageUsage, referenceId, e);
		}
	}

} 