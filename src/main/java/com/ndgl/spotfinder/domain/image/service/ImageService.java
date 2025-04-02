package com.ndgl.spotfinder.domain.image.service;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.image.dto.ImageRequest;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponse;
import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.global.aws.s3.S3Service;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.util.Ut;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
	private final ImageRepository imageRepository;
	private final S3Service s3Service;

	/**
	 * 이미지 업로드를 위한 Presigned URL 목록 생성
	 */
	public PresignedUrlsResponse createImage(ImageRequest rq) {
		try {
			List<URL> urls = s3Service.generatePresignedUrls(rq.imageType(), rq.id(), rq.imageExtensions());
			return new PresignedUrlsResponse(rq.id(), urls);
		} catch (DataIntegrityViolationException e) {
			throw ErrorCode.S3_OBJECT_UPLOAD_FAIL.throwServiceException();
		}
	}

	/**
	 * 특정 객체의 모든 이미지를 조회하고 Presigned URL을 생성하여 반환
	 */
	@Transactional(readOnly = true)
	public PresignedUrlsResponse findImagesWithPresignedUrls(ImageType imageType, long id) {
		List<Image> images = imageRepository.findByImageTypeAndReferenceId(imageType, id);

		List<URL> presignedUrls = images.stream()
			.map(image -> {
				try {
					return s3Service.generatePresignedGetUrl(image.getUrl());
				} catch (Exception e) {
					log.error("URL 변환 중 오류 발생: {}", e.getMessage());
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		return new PresignedUrlsResponse(id, presignedUrls);
	}

	/**
	 * 이미지 URL 목록을 받아서 DB에 저장
	 */
	@Transactional
	public void saveImages(ImageType imageType, long id, List<String> imageUrls) {
		if (Ut.list.hasValue(imageUrls)) {
			List<Image> images = imageUrls.stream()
				.map(url -> Image.builder()
					.imageType(ImageType.POST)
					.url(url)
					.imageType(imageType)
					.referenceId(id)
					.build())
				.collect(Collectors.toList());

			imageRepository.saveAll(images);
		}
	}

	/**
	 * 단일 이미지 삭제
	 */
	@Transactional
	public void deleteImage(Long imageId) {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(ErrorCode.IMAGE_NOT_FOUND::throwServiceException);

		s3Service.deleteFile(image.getUrl());
		imageRepository.deleteById(imageId);
	}

	/**
	 * 해당 객체의 모든 이미지 삭제 (이미지 엔티티, S3 객체)
	 */
	@Transactional
	public void deletePostWithAllImages(ImageType imageType, long id) {
		imageRepository.deleteAllByImageTypeAndReferenceId(imageType, id);
	}
}