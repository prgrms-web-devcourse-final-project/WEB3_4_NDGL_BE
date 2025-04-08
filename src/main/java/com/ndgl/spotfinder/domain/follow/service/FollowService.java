package com.ndgl.spotfinder.domain.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	public void createFollow(String email, Long followingId) {
		User follower = userService.findUserByEmail(email);
		User followee = userService.findUserById(followingId);

		checkIfFollowerEqualsFollowee(follower, followee);
		checkIfAlreadyFollowing(follower, followee);

		Follow follow = Follow.builder()
			.follower(follower)
			.followee(followee)
			.build();

		followRepository.save(follow);
	}

	@Transactional
	public void deleteFollow(String email, Long unfollowingId) {
		User follower = userService.findUserByEmail(email);
		User followee = userService.findUserById(unfollowingId);

		checkIfFollowerEqualsFollowee(follower, followee);
		checkIfNotFollowing(follower, followee);
		
		followRepository.deleteFollowByFollowerAndFollowee(follower, followee);
	}

	private void checkIfFollowerEqualsFollowee(User follower, User followee) {
		if (follower.equals(followee)) {
			ErrorCode.FOLLOWER_EQUALS_FOLLOWEE.throwServiceException();
		}
	}

	private void checkIfAlreadyFollowing(User follower, User followee) {
		if (isFollowed(follower, followee)) {
			ErrorCode.ALREADY_FOLLOWED.throwServiceException();
		}
	}

	private void checkIfNotFollowing(User follower, User followee) {
		if (!isFollowed(follower, followee)) {
			ErrorCode.NOT_FOLLOWED.throwServiceException();
		}
	}

	public Boolean isFollowed(User follower, User followee) {
		return followRepository.existsFollowByFollowerAndFollowee(follower, followee);
	}
}
