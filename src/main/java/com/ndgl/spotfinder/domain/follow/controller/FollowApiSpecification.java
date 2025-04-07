package com.ndgl.spotfinder.domain.follow.controller;

import java.security.Principal;

import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "팔로우")
public interface FollowApiSpecification {
	@Operation(
		summary = "유저 팔로우",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<Void> followUser(
		Long followingId,
		Principal principal
	);

	@Operation(
		summary = "유저 언팔로우",
		responses = {
			@ApiResponse(responseCode = "200", description = "성공", content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
			))
		}
	)
	RsData<Void> unfollowUser(
		Long unfollowingId,
		Principal principal
	);
}
