package com.ndgl.spotfinder.global.common.dto;

import java.util.List;

public record SliceResponse<T>(
	List<T> contents,
	boolean hasNext
) {
}
