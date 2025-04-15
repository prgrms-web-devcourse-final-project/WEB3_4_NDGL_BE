package com.ndgl.spotfinder.domain.blog.service;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.blog.dto.BlogResponseDto;
import com.ndgl.spotfinder.domain.blog.dto.PostSummaryDto;
import com.ndgl.spotfinder.domain.follow.service.FollowService;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogService {
	private final UserService userService;
	private final PostService postService;
	private final FollowService followService;

	private static final Integer DEFAULT_PREVIEW_POST_COUNT = 3;

	@Transactional(readOnly = true)
	public SliceResponse<BlogResponseDto> getBlogs(SliceRequest sliceRequest, String email) {
		User currentUser = (email != null) ? userService.findUserByEmail(email) : null;
		Slice<User> users = userService.findUsers(sliceRequest);

		List<BlogResponseDto> posts = users.stream()
			.filter(user -> user != currentUser)
			.map(user -> new BlogResponseDto(
				user.getId(),
				user.getBlogName(),
				user.getNickName(),
				isFollowedUser(currentUser, user),
				getPostsByUser(user)
			))
			.toList();

		return new SliceResponse<>(
			posts,
			users.hasNext()
		);
	}

	private Boolean isFollowedUser(User follower, User followee) {
		if (follower == null) {
			return false;
		}

		return followService.isFollowed(follower, followee);
	}

	@Transactional(readOnly = true)
	public List<PostSummaryDto> getPostsByUser(User user) {
		return postService.getPostsByUser(user.getId(), DEFAULT_PREVIEW_POST_COUNT)
			.stream()
			.map(post -> new PostSummaryDto(
				post.getId(),
				post.getTitle()
			))
			.toList();
	}
}
