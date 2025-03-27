package com.ndgl.spotfinder.global.aws.s3;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Presigner s3Presigner;
	private final S3Client s3Client;

	@Value("${spring.cloud.aws.s3.bucket}")
	String bucketName;

	@Value("${spring.cloud.aws.region.static}")
	String region;

	/**
	 * 파일을 직접 S3에 업로드하고 URL을 반환합니다
	 *
	 * @param postId 게시글 ID
	 * @param files  업로드할 파일들
	 * @return 업로드된 파일들의 URL 목록
	 */
	public List<String> uploadFiles(long postId, List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> uploadedUrls = new ArrayList<>();

		for (MultipartFile file : files) {
			if (file.isEmpty())
				continue;

			try {
				// 파일 이름과 확장자 추출
				String originalFilename = file.getOriginalFilename();
				String extension = getExtension(originalFilename);

				// 고유한 파일명 생성 (postId/UUID.확장자)
				String objectKey = postId + "/" + UUID.randomUUID() + "." + extension;

				// ContentType 설정
				String contentType = file.getContentType();
				if (contentType == null) {
					contentType = "application/octet-stream";
				}

				// S3에 업로드
				PutObjectRequest putRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(objectKey)
					.contentType(contentType)
					.build();

				s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

				// 업로드된 파일의 URL 생성
				String uploadedUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
					bucketName, region, objectKey);
				uploadedUrls.add(uploadedUrl);

			} catch (Exception e) {
				throw ErrorCode.S3_OBJECT_UPLOAD_FAIL.throwS3Exception(e);
			}
		}

		return uploadedUrls;
	}

	/**
	 * 파일 확장자를 추출합니다
	 */
	private String getExtension(String filename) {
		if (filename == null) {
			return "";
		}
		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex < 0) {
			return "";
		}
		return filename.substring(lastDotIndex + 1);
	}

	/**
	 * 이미지 조회를 위한 서명된 URL 생성 (10분 유효)
	 *
	 * @param imageUrl 원본 S3 이미지 URL
	 * @return 10분간 유효한 서명된 URL
	 */
	public String generatePresignedGetUrl(String imageUrl) {
		// URL에서 키 추출
		String objectKey = S3Util.extractObjectKeyFromUrl(imageUrl);
		if (objectKey == null) {
			throw ErrorCode.S3_INVALID_URL.throwServiceException();
		}

		try {
			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.getObjectRequest(getObjectRequest -> getObjectRequest
					.bucket(bucketName)
					.key(objectKey))
				.signatureDuration(Duration.ofMinutes(10))
				.build();

			return s3Presigner.presignGetObject(presignRequest).url().toString();
		} catch (SdkException e) {
			throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e);
		}
	}
}
