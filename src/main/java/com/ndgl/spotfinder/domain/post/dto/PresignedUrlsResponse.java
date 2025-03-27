package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

public record PresignedUrlsResponse(
	long postId,
	List<String> presignedUrls
) {
}
