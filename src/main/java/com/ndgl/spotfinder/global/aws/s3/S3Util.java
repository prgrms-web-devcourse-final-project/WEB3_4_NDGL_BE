package com.ndgl.spotfinder.global.aws.s3;

import java.util.UUID;

import com.ndgl.spotfinder.domain.image.type.ImageType;

public class S3Util {

	public static String extractKeyFromUrl(String url) {
		int domainEndIndex = url.indexOf(".com/");
		return url.substring(domainEndIndex + 5);
	}

	public static String buildS3Key(ImageType imageType, long id, String fileType) {
		String fileName = UUID.randomUUID() + "." + fileType;

		return switch (imageType) {
			case POST -> "posts/" + id + "/" + fileName;
		};
	}

}
