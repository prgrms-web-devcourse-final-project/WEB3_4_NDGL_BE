package com.ndgl.spotfinder.domain.image.dto;

import java.net.URL;
import java.util.List;

public record PresignedUrlsResponse(
	long id,
	List<URL> presignedUrls
) {
}