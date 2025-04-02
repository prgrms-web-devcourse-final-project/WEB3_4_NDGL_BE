package com.ndgl.spotfinder.domain.post.dto;

import com.ndgl.spotfinder.domain.post.entity.Location;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LocationDto(
	@NotBlank(message = "장소 이름은 필수입니다.")
	String name,

	@NotBlank(message = "장소 주소는 필수입니다.")
	String address,

	@DecimalMin(value = "-90.0", message = "위도는 -90 이상입니다.")
	@DecimalMax(value = "90.0", message = "위도는 90 이하입니다.")
	Double latitude,

	@DecimalMin(value = "-180.0", message = "경도는 -180 이상입니다.")
	@DecimalMax(value = "180.0", message = "경도는 180 이하입니다.")
	Double longitude,

	@Min(value = 1, message = "순서는 1 이상입니다.")
	@Max(value = 20, message = "순서는 20 이하입니다.")
	Integer sequence
) {
	public LocationDto(Location location) {
		this(
			location.getName(),
			location.getAddress(),
			location.getLatitude(),
			location.getLongitude(),
			location.getSequence()
		);
	}
	
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
