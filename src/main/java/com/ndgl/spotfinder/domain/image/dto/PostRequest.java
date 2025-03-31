package com.ndgl.spotfinder.domain.image.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record PostRequest(
	List<String> imageExtensions
) {
	public PostRequest {
		imageExtensions = Objects.requireNonNullElse(imageExtensions, new ArrayList<>());
	}
}
