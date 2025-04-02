package com.ndgl.spotfinder.domain.image.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ndgl.spotfinder.domain.image.type.ImageType;

public record ImageRequest(
	long id,
	ImageType imageType,
	List<String> imageExtensions
) {
	public ImageRequest {
		imageExtensions = Objects.requireNonNullElse(imageExtensions, new ArrayList<>());
	}
}
