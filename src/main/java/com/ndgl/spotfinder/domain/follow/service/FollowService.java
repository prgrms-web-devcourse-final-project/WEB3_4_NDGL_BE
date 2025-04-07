package com.ndgl.spotfinder.domain.follow.service;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.follow.entity.Follow;
import com.ndgl.spotfinder.domain.follow.repository.FollowRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {
	private final FollowRepository followRepository;
	private final UserService userService;

	public void createFollow(String email, Long followingId) {
		User follower = userService.findUserByEmail(email);
		User followee = userService.findUserById(followingId);

		checkIfAlreadyFollowed(follower, followee);

		Follow follow = Follow.builder()
			.follower(follower)
			.followee(followee)
			.build();

		followRepository.save(follow);
	}

	private void checkIfAlreadyFollowed(User follower, User followee) {
		Boolean isFollowed = followRepository.existsFollowByFollowerAndFollowee(follower, followee);

		if (isFollowed) {
			ErrorCode.ALREADY_FOLLOWED.throwServiceException();
		}
	}
}
