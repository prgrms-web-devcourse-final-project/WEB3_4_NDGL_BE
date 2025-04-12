package com.ndgl.spotfinder.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenRequestDto(
	@JsonProperty("grant_type") String grantType,
	@JsonProperty("client_id") String clientId,
	@JsonProperty("client_secret") String clientSecret,
	String code,
	@JsonProperty("redirect_uri") String redirectUri
) {
}
