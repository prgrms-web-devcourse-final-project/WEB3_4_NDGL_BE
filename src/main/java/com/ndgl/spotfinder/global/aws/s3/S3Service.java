package com.ndgl.spotfinder.global.aws.s3;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.util.Ut;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

	private final S3Presigner s3Presigner;
	private final S3Client s3Client;

	@Value("${spring.cloud.aws.s3.bucket}")
	String bucketName;

	private static final int EXPIRATION_MINUTES = 2; // 만료 2분

	/**
	 * 여러 파일에 대한 업로드용 Presigned URL 목록 생성
	 *
	 * @param imageType      이미지 유형
	 * @param id             이미지와 연관된 객체 ID
	 * @param fileExtensions 파일 확장자 목록 (jpg, png 등)
	 * @return 생성된 Presigned URL 목록
	 */
	public List<URL> generatePresignedUrls(ImageType imageType, long id, List<String> fileExtensions) {
		if (!Ut.list.hasValue(fileExtensions))
			return List.of();

		try {
			return fileExtensions.stream()
				.map(fileExtension -> generatePresignedUrl(imageType, id, fileExtension))
				.toList();
		} catch (SdkException e) {
			throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * 단일 파일에 대한 업로드용 Presigned URL 생성
	 *
	 * @param imageType     이미지 유형 (POST, PROFILE 등)
	 * @param id            이미지와 연관된 객체 ID (게시글 ID 등)
	 * @param fileExtension 파일 확장자 (jpg, png 등)
	 * @return 생성된 Presigned URL
	 */
	public URL generatePresignedUrl(ImageType imageType, long id, String fileExtension) {
		String key = S3Util.buildS3Key(imageType, id, fileExtension);

		try {
			PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder -> builder
				.putObjectRequest(putObject -> putObject
					.bucket(bucketName)
					.key(key))
				.signatureDuration(Duration.ofMinutes(EXPIRATION_MINUTES)));

			log.debug("Presigned URL 생성");
			return presignedRequest.url();
		} catch (SdkException e) {
			throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * 조회용 Presigned URL 생성
	 *
	 * @param imageUrl 원본 S3 이미지 URL
	 * @return 지정된 시간 동안 유효한 조회용 서명된 URL
	 */
	@Deprecated
	public URL generatePresignedGetUrl(String imageUrl) {
		try {
			String objectKey = S3Util.extractObjectKeyFromUrl(imageUrl);

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.getObjectRequest(getObjectRequest -> getObjectRequest
					.bucket(bucketName)
					.key(objectKey))
				.signatureDuration(Duration.ofMinutes(EXPIRATION_MINUTES))
				.build();

			return s3Presigner.presignGetObject(presignRequest).url();
		} catch (SdkException e) {
			throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * 단일 S3 객체 삭제
	 *
	 * @param imageUrl 삭제할 이미지 URL
	 */
	public void deleteFile(String imageUrl) {
		try {
			String objectKey = S3Util.extractObjectKeyFromUrl(imageUrl);

			DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.build();

			s3Client.deleteObject(deleteRequest);
			log.debug("S3 파일 삭제 완료: {}", objectKey);
		} catch (SdkException e) {
			throw ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * URL 리스트를 받아 Object 모두 삭제
	 *
	 * @param urls url 리스트
	 */
	public void deleteObjectsByUrls(List<String> urls) {
		if (!Ut.list.hasValue(urls))
			return;

		try {
			List<ObjectIdentifier> objectIdentifiers = urls.stream()
				.map(S3Util::extractObjectKeyFromUrl)
				.map(key -> ObjectIdentifier.builder().key(key).build())
				.toList();

			DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
				.bucket(bucketName)
				.delete(delete -> delete.objects(objectIdentifiers))
				.build();

			// S3 객체 삭제 요청
			s3Client.deleteObjects(deleteRequest);

		} catch (SdkException e) {
			ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * S3의 폴더의 모든 Object 들 삭제
	 *
	 * @param imageType 타입
	 * @param id        ID
	 */
	public void deleteAllObjectsById(ImageType imageType, long id) {
		// folderPath 로 변환
		String folderPath = S3Util.getFolderPath(imageType, id);

		try {
			// 폴더 내 모든 객체 목록 조회
			List<S3Object> objects = listAllObjectsInFolder(folderPath);

			// 객체가 없으면 종료
			if (objects.isEmpty()) {
				return;
			}

			// 삭제할 객체 키 목록 생성
			List<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
			for (S3Object object : objects) {
				objectIdentifiers.add(
					ObjectIdentifier.builder()
						.key(object.key())
						.build()
				);
			}

			// 객체 삭제 요청 (한 번에 최대 1000개 객체 삭제 가능)
			DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
				.bucket(bucketName)
				.delete(delete -> delete.objects(objectIdentifiers))
				.build();

			// S3 객체 삭제 요청
			s3Client.deleteObjects(deleteRequest);

		} catch (SdkException e) {
			ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * 폴더의 모든 Object 조회
	 *
	 * @param folderPath 폴더 경로
	 * @return Object
	 */
	private List<S3Object> listAllObjectsInFolder(String folderPath) {
		List<S3Object> objects = new ArrayList<>();

		try {
			ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
				.bucket(bucketName)
				.prefix(folderPath)
				.build();

			ListObjectsV2Response listResponse;
			do {
				listResponse = s3Client.listObjectsV2(listRequest);
				objects.addAll(listResponse.contents());
				listRequest = ListObjectsV2Request.builder()
					.bucket(bucketName)
					.prefix(folderPath)
					.continuationToken(listResponse.nextContinuationToken())
					.build();
			} while (Boolean.TRUE.equals(listResponse.isTruncated()));
		} catch (SdkException e) {
			ErrorCode.S3_OBJECT_ACCESS_FAIL.throwS3Exception(e);
		}

		return objects;
	}
}
