package com.ndgl.spotfinder.domain.follow.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.follow.FollowCheckResponseDto;
import com.ndgl.spotfinder.domain.follow.service.FollowService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follow")
public class FollowController implements FollowApiSpecification {
	private final FollowService followService;

	@PostMapping("/{followingId}")
	public RsData<Void> followUser(@PathVariable Long followingId, Principal principal) {
		followService.createFollow(principal.getName(), followingId);

		return RsData.success(HttpStatus.OK);
	}

	@DeleteMapping("/{unfollowingId}")
	public RsData<Void> unfollowUser(@PathVariable Long unfollowingId, Principal principal) {
		followService.deleteFollow(principal.getName(), unfollowingId);

		return RsData.success(HttpStatus.OK);
	}

	@GetMapping("/{userId}")
	public RsData<FollowCheckResponseDto> checkFollow(@PathVariable Long userId, Principal principal) {
		FollowCheckResponseDto responseDto = followService.getFollow(principal.getName(), userId);

		return RsData.success(HttpStatus.OK, responseDto);
	}
}
