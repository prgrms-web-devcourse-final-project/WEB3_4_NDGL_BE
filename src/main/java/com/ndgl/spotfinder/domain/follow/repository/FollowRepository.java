package com.ndgl.spotfinder.domain.follow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.follow.entity.Follow;
import com.ndgl.spotfinder.domain.user.entity.User;

public interface FollowRepository extends JpaRepository<Follow, Long> {
	Boolean existsFollowByFollowerAndFollowee(User follower, User following);

	Boolean existsNotFollowByFollowerAndFollowee(User follower, User followee);
	
	void deleteFollowByFollowerAndFollowee(User follower, User followee);
}
