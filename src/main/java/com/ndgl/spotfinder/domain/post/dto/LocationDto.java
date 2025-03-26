package com.ndgl.spotfinder.domain.post.dto;

import com.ndgl.spotfinder.domain.post.entity.Location;

public record LocationDto(
	String name,
	String address,
	Double latitude,
	Double longitude,
	Integer sequence
) {
	public Location toLocation() {
		return Location.builder()
			.name(name)
			.address(address)
			.latitude(latitude)
			.longitude(longitude)
			.sequence(sequence)
			.build();
	}
}
