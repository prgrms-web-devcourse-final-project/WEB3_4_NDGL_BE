package com.ndgl.spotfinder.domain.image.dto;

public record PresignedImageResponse(
	long id,
	String presignedUrl
) {	
	public static PresignedImageResponse of(long imageId, String presignedUrl) {
		return new PresignedImageResponse(imageId, presignedUrl);
	}
} 