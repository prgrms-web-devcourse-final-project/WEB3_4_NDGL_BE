package com.ndgl.spotfinder.global.aws.s3;

import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;

import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.global.util.Ut;

public class S3Util {

	public static String extractKeyFromUrl(String url) {
		int domainEndIndex = url.indexOf(".com/");
		return url.substring(domainEndIndex + 5);
	}

	public static String buildS3Key(long id, MultipartFile file, ImageType type, Environment environment) {
		String originalFilename = file.getOriginalFilename();
		String extension = getExtension(originalFilename);
		String rootFolder = Ut.isTestMode(environment) ? ImageType.TEST.name() + "/" : "PROD/" + type.name();

		return String.format("%s/%d/%s.%s", rootFolder, id, UUID.randomUUID(), extension);
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
