package com.ndgl.spotfinder.global.aws.s3;

import java.util.UUID;

import com.ndgl.spotfinder.domain.image.type.ImageType;

public class S3Util {

	public static String extractObjectKeyFromUrl(String url) {
		int domainEndIndex = url.indexOf(".com/");
		if (domainEndIndex != -1) {
			return url.substring(domainEndIndex + 5); // ".com/" 의 길이인 5를 더해줍니다
		}
		return null;
	}

	public static String buildS3Key(ImageType imageType, long id, String fileType) {
		String fileName = UUID.randomUUID() + "." + fileType;

		return switch (imageType) {
			case POST -> "posts/" + id + "/" + fileName;
		};
	}

	public static String getFolderPath(ImageType imageType, long id) {
		String type = switch(imageType) {
			case POST -> "posts";
		};

		return type + "/" + id + "/";
	}

}
