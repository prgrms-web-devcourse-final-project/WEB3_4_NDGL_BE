package com.ndgl.spotfinder.global.aws.s3;

import java.util.UUID;

import com.ndgl.spotfinder.domain.image.type.ImageUsage;

public class S3Util {

	public static String extractObjectKeyFromUrl(String url) {
		int domainEndIndex = url.indexOf(".com/");
		if (domainEndIndex != -1) {
			return url.substring(domainEndIndex + 5); // ".com/" 의 길이인 5를 더해줍니다
		}
		return null;
	}

	public static String buildS3Key(ImageUsage imageUsage, long id, String fileType) {
		String fileName = UUID.randomUUID() + "." + fileType;

		return switch (imageUsage) {
			case POST -> imageUsage.name() + "/" + id + "/" + fileName;
		};
	}

	public static String getFolderPath(ImageUsage imageUsage, long id) {
		String type = imageUsage.name();
		return type + "/" + id + "/";
	}

}
