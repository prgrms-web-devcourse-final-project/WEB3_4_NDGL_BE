package com.ndgl.spotfinder.domain.image.service;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.image.dto.ImageUrlRequestDto;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponseDto;
import com.ndgl.spotfinder.domain.image.dto.UploadCompleteRequestDto;
import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageUsage;
import com.ndgl.spotfinder.global.aws.s3.S3Service;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.common.util.CommonUtil;

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
	public PresignedUrlsResponseDto createImage(ImageUrlRequestDto rq) {
		try {
			List<URL> urls = s3Service.generatePresignedUrls(rq.imageUsage(), rq.referenceId(), rq.imageExtensions());
			return new PresignedUrlsResponseDto(urls);
		} catch (DataIntegrityViolationException e) {
			throw ErrorCode.S3_OBJECT_UPLOAD_FAIL.throwServiceException();
		}
	}

	/**
	 * 이미지 URL 목록을 받아서 DB에 저장
	 */
	@Transactional
	public void saveImages(UploadCompleteRequestDto rq) {
		if (CommonUtil.list.hasValue(rq.imageUrl())) {
			List<Image> images = rq.imageUrl().stream()
				.map(url -> Image.builder()
					.imageUsage(ImageUsage.POST)
					.url(url)
					.imageUsage(rq.imageUsage())
					.referenceId(rq.id())
					.build())
				.collect(Collectors.toList());

			imageRepository.saveAll(images);
		}
	}

	/**
	 * 단일 이미지 삭제
	 *
	 * @param imageUrl 이미지 url
	 */
	@Transactional
	public void deleteImageByUrl(String imageUrl) {
		s3Service.deleteFile(imageUrl);
		imageRepository.findByUrl(imageUrl).ifPresent(imageRepository::delete);
	}

	/**
	 * 해당 객체의 모든 이미지 삭제 (이미지 엔티티, S3 객체)
	 */
	@Transactional
	public void deletePostWithAllImages(ImageUsage imageUsage, long referenceId) {
		s3Service.deleteAllObjectsById(imageUsage, referenceId);
		imageRepository.deleteAllByImageUsageAndReferenceId(imageUsage, referenceId);
	}
}