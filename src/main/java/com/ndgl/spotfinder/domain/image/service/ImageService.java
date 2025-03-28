package com.ndgl.spotfinder.domain.image.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ndgl.spotfinder.domain.image.dto.PresignedImageResponse;
import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.global.aws.s3.S3Service;
import com.ndgl.spotfinder.global.util.Ut;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {
	private final ImageRepository imageRepository;
	private final S3Service s3Service;

	/**
	 * 특정 게시글의 모든 이미지를 조회하고 Presigned 반환
	 *
	 * @param postId 게시글 ID
	 * @return 값이 담긴 DTO 반환
	 */
	@Transactional(readOnly = true)
	public List<PresignedImageResponse> findImagesWithPresignedUrls(long postId) {
		List<Image> images = imageRepository.findByPostId(postId);

		return images.stream()
			.map(image -> {
				String presignedUrl = s3Service.generatePresignedGetUrl(image.getUrl());
				return PresignedImageResponse.of(image.getId(), presignedUrl);
			})
			.collect(Collectors.toList());
	}

	/**
	 * 이미지 파일을 S3에 업로드하고 URL을 DB에 저장
	 *
	 * @param postId 게시글 ID
	 * @param files  업로드할 이미지 파일들
	 */
	@Transactional
	public void uploadAndSaveImages(long postId, List<MultipartFile> files) {
		List<String> imageUrls = s3Service.uploadFiles(postId, files);

		saveImages(postId, imageUrls);
	}

	/**
	 * 이미지 URL 목록을 받아서 DB에 저장
	 *
	 * @param postId    게시글 ID
	 * @param imageUrls 이미지 URL 목록
	 */
	@Transactional
	public void saveImages(long postId, List<String> imageUrls) {
		if (!Ut.list.hasValue(imageUrls)) {
			return;
		}

		List<Image> images = imageUrls.stream()
			.map(url -> Image.builder()
				.postId(postId)
				.url(url)
				.build())
			.collect(Collectors.toList());

		imageRepository.saveAll(images);
	}

	public void deleteImage(Long imageId, Long postId) {


		imageRepository.deleteById(imageId);
	}
}