package com.ndgl.spotfinder.global.aws.s3;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.util.Ut;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
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

	private static final int EXPIRATION_MINUTES = 3; // 3분

	/**
	 * 다중 파일 S3 업로드 및 URL 반환
	 *
	 * @param id    ID
	 * @param files 업로드할 파일들
	 * @return 업로드된 파일들의 URL 목록
	 */
	public List<String> uploadFiles(long id, List<MultipartFile> files) {
		if (!Ut.list.hasValue(files))
			return Collections.emptyList();

		return files.stream()
			.filter(file -> !file.isEmpty())
			.map(file -> uploadFile(id, file))
			.toList();
	}

	/**
	 * 단일 파일 S3 업로드
	 *
	 * @param id   폴더 ID
	 * @param file 파일
	 * @return URL
	 */
	private String uploadFile(long id, MultipartFile file) {
		try {
			String key = S3Util.buildS3Key(id, file);

			s3Client.putObject(
				PutObjectRequest.builder()
					.bucket(bucketName)
					.key(key)
					.build(),
				RequestBody.fromInputStream(file.getInputStream(), file.getSize())
			);

			return S3Util.generateS3Url(bucketName, region, key);
		} catch (Exception e) {
			throw ErrorCode.S3_OBJECT_UPLOAD_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * 서명된 URL 생성
	 *
	 * @param imageUrl 원본 S3 이미지 URL
	 * @return 지정된 시간 동안 유효한 서명된 URL
	 */
	public String generatePresignedGetUrl(String imageUrl) {
		try {
			String objectKey = S3Util.extractKeyFromUrl(imageUrl);

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.getObjectRequest(getObjectRequest -> getObjectRequest
					.bucket(bucketName)
					.key(objectKey))
				.signatureDuration(Duration.ofMinutes(EXPIRATION_MINUTES))
				.build();

			return s3Presigner.presignGetObject(presignRequest).url().toString();
		} catch (SdkException e) {
			throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * postId에 해당하는 폴더의 모든 객체 조회
	 *
	 * @param postId 게시글 ID
	 * @return S3 객체 목록
	 */
	public List<S3Object> listObjectsByPostId(long postId) {
		String folderPath = postId + "/";
		return listAllObjectsInFolder(folderPath);
	}

	/**
	 * 단일 S3 객체 삭제
	 *
	 * @param imageUrl 삭제할 이미지 URL
	 */
	public void deleteFile(String imageUrl) {
		try {
			String key = S3Util.extractKeyFromUrl(imageUrl);

			DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.build();

			s3Client.deleteObject(deleteRequest);
		} catch (SdkException e) {
			throw ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * postId에 해당하는 폴더의 모든 객체 삭제
	 *
	 * @param postId 게시글 ID
	 */
	public void deleteAllObjectsByPostId(long postId) {
		String folderPath = postId + "/";

		try {
			// 폴더 내 모든 객체 목록 조회
			List<S3Object> objects = listAllObjectsInFolder(folderPath);

			// 객체가 없으면 종료
			if (objects.isEmpty()) {
				return;
			}

			// 삭제할 객체 키 목록 생성
			List<ObjectIdentifier> objectIdentifiers = objects.stream()
				.map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
				.toList();

			// 객체 삭제 요청 (한 번에 최대 1000개 객체 삭제 가능)
			DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
				.bucket(bucketName)
				.delete(delete -> delete.objects(objectIdentifiers))
				.build();

			// S3 객체 삭제 요청
			s3Client.deleteObjects(deleteRequest);

		} catch (SdkException e) {
			throw ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e);
		}
	}

	/**
	 * 폴더의 모든 Object 조회
	 *
	 * @param folderPath 폴더 경로
	 * @return S3 객체 목록
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

				// 결과가 더 있는 경우 다음 페이지 요청
				if (Boolean.TRUE.equals(listResponse.isTruncated())) {
					listRequest = ListObjectsV2Request.builder()
						.bucket(bucketName)
						.prefix(folderPath)
						.continuationToken(listResponse.nextContinuationToken())
						.build();
				}
			} while (Boolean.TRUE.equals(listResponse.isTruncated()));
		} catch (SdkException e) {
			throw ErrorCode.S3_OBJECT_ACCESS_FAIL.throwS3Exception(e);
		}

		return objects;
	}
}
