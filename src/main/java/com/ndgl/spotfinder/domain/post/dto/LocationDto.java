package com.ndgl.spotfinder.domain.post.dto;

import com.ndgl.spotfinder.domain.post.entity.Location;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LocationDto(
	@NotBlank
	String name,

	@NotBlank
	String address,

	@DecimalMin("-90.0")
	@DecimalMax("90.0")
	Double latitude,

	@DecimalMin("-180.0")
	@DecimalMax("180.0")
	Double longitude,

	@Min(1)
	@Max(20)
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
