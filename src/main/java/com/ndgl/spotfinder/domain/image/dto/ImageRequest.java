package com.ndgl.spotfinder.domain.image.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ImageRequest(
	long id,
	List<String> imageExtensions
) {
	public ImageRequest {
		imageExtensions = Objects.requireNonNullElse(imageExtensions, new ArrayList<>());
	}
}
