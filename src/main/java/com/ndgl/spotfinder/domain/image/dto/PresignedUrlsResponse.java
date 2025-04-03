package com.ndgl.spotfinder.domain.image.dto;

import java.net.URL;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedUrlsResponse(
	@Schema(
		description = "S3 등록 URL",
		example = "[\"https://ndgl-spot-finder-aws.s3.ap-northeast-2.amazonaws.com/{imageType}/${referenceId}\", "
			+ "\"https://ndgl-spot-finder-aws.s3.ap-northeast-2.amazonaws.com/{imageType}/${referenceId}\"]"
	)
	List<URL> presignedUrls
) {
}
