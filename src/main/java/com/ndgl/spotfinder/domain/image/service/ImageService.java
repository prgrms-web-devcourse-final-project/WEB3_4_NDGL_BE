package com.ndgl.spotfinder.domain.image.service;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.image.dto.ImageRequest;
import com.ndgl.spotfinder.domain.image.dto.PresignedImageResponse;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponse;
import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.global.aws.s3.S3Service;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.util.Ut;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Profile("!test")
public class ImageService {
	private final PostRepository postRepository;
	private final ImageRepository imageRepository;
	private final S3Service s3Service;

	/**
	 * 이미지 업로드를 위한 Presigned URL 목록 생성
	 *
	 * @param imageRequest 이미지 확장자 정보를 포함한 요청 객체
	 * @return 생성된 Presigned URL 목록과 게시글 ID를 포함한 응답 객체
	 */
	public PresignedUrlsResponse createImage(ImageRequest imageRequest) {
		try {
			Post post = postRepository.findById(1L) // TODO 하드코딩 된 ID 변경
				.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);
			return createPresignedUrls(post.getId(), imageRequest.imageExtensions());
		} catch (DataIntegrityViolationException e) {
			throw ErrorCode.S3_OBJECT_UPLOAD_FAIL.throwServiceException();
		}
	}

	/**
	 * 특정 게시글의 모든 이미지를 조회하고 Presigned URL을 생성하여 반환
	 *
	 * @param postId 게시글 ID
	 * @return 이미지 ID와 Presigned URL이 포함된 응답 객체 목록
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

		Post post = postRepository.findById(postId)
			.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);

		List<Image> images = imageUrls.stream()
			.map(url -> Image.builder()
				.post(post)
				.url(url)
				.build())
			.collect(Collectors.toList());

		imageRepository.saveAll(images);
	}

	/**
	 * 이미지 삭제
	 *
	 * @param imageId 이미지 ID
	 */
	public void deleteImage(Long imageId) {
		String url = imageRepository.findById(imageId).get().getUrl();
		s3Service.deleteFile(url);
		imageRepository.deleteById(imageId);
	}

	/**
	 * ID와 확장자 기반으로 S3 Presigned URL 목록 생성
	 *
	 * @param id         ID
	 * @param extensions 파일 확장자 목록
	 * @return 생성된 Presigned URL 목록과 게시글 ID를 포함한 응답 객체
	 */
	private PresignedUrlsResponse createPresignedUrls(long id, List<String> extensions) {
		List<URL> urls = s3Service.generatePresignedUrls(ImageType.POST, id, extensions);
		return new PresignedUrlsResponse(id, urls);
	}

}