package com.ndgl.spotfinder.global.aws.s3;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class S3Util {

	public static String extractKeyFromUrl(String url) {
		int domainEndIndex = url.indexOf(".com/");
		return url.substring(domainEndIndex + 5);
	}

	public static String buildS3Key(long id, MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		String extension = getExtension(originalFilename);

		return String.format("post/%d/%s.%s", id, UUID.randomUUID(), extension); // 만일 확장되면 type으로 구분할 것
	}

	public static String getExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');

		if (lastDotIndex < 0)
			return "";

		return filename.substring(lastDotIndex + 1);
	}

	public static String generateS3Url(String bucketName, String region, String key) {
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
	}

}
