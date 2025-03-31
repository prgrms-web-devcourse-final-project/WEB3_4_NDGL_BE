package com.ndgl.spotfinder.domain.image.service;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.image.dto.ImageRequest;
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
	private static final Logger log = LoggerFactory.getLogger(ImageService.class);

	/**
	 * 이미지 업로드를 위한 Presigned URL 목록 생성
	 *
	 * @param imageRequest 이미지 확장자 정보를 포함한 요청 객체
	 * @return 생성된 Presigned URL 목록과 게시글 ID를 포함한 응답 객체
	 */
	public PresignedUrlsResponse createImage(ImageRequest imageRequest) {
		try {
			Post post = postRepository.findById(1L)
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
	public PresignedUrlsResponse findImagesWithPresignedUrls(long postId) {
		List<Image> images = imageRepository.findByPostId(postId);

		List<URL> presignedUrls = images.stream()
			.map(image -> {
				try {
					return s3Service.generatePresignedGetUrl(image.getUrl());
				} catch (Exception e) {
					log.error("URL 변환 중 오류 발생: {}", e.getMessage());
					return null;
				}
			})
			.filter(url -> url != null)
			.collect(Collectors.toList());

		return new PresignedUrlsResponse(postId, presignedUrls);
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
	 * 게시물의 모든 이미지 삭제 (DB + S3)
	 * 단, 게시물 자체는 삭제하지 않음
	 *
	 * @param postId 게시물 ID
	 */
	@Transactional
	public void deleteAllImages(long postId) {
		// 게시물 존재 확인
		postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다. ID: " + postId));

		// 1. 게시물에 연결된 이미지 엔티티들 찾기
		List<Image> images = imageRepository.findAllByPostId(postId);

		if (images.isEmpty()) {
			log.info("게시물 ID {}에 연결된 이미지가 없습니다.", postId);
			return;
		}

		// 2. 이미지 엔티티 삭제
		imageRepository.deleteAllByPostId(postId);
		log.info("게시물 ID {}의 이미지 엔티티 {}개 삭제 완료", postId, images.size());
	}

	/**
	 * 게시물과 연관된 모든 것 삭제 (이미지 엔티티, S3 객체, 게시물 엔티티)
	 *
	 * @param postId 게시물 ID
	 */
	@Transactional
	public void deletePostWithAllImages(long postId) {
		// 게시물 조회
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다. ID: " + postId));

		// 1. S3에서 이미지 객체들 삭제
		s3Service.deleteAllObjectsById(ImageType.POST, postId);
		log.info("게시물 ID {}의, S3 객체 삭제 완료", postId);

		// 2. 이미지 엔티티들 삭제
		imageRepository.deleteAllByPostId(postId);

		// 3. 마지막으로 게시물 삭제
		postRepository.delete(post);
		log.info("게시물 ID {} 삭제 완료", postId);
	}

	/**
	 * ID와 확장자 기반으로 S3 Presigned URL 목록 생성
	 *
	 * @param postId     포스트 ID
	 * @param extensions 파일 확장자 목록
	 * @return 생성된 Presigned URL 목록과 게시글 ID를 포함한 응답 객체
	 */
	private PresignedUrlsResponse createPresignedUrls(long postId, List<String> extensions) {
		List<URL> urls = s3Service.generatePresignedUrls(ImageType.POST, postId, extensions);
		return new PresignedUrlsResponse(postId, urls);
	}

}