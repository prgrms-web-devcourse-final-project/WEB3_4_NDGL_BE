package com.ndgl.spotfinder.domain.blog.service;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.blog.dto.BlogResponseDto;
import com.ndgl.spotfinder.domain.blog.dto.PostSummeryDto;
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

	public SliceResponse<BlogResponseDto> getBlogs(SliceRequest sliceRequest, String email) {
		User currentUser = userService.findUserByEmail(email);
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
		return followService.isFollowed(follower, followee);
	}

	private List<PostSummeryDto> getPostsByUser(User user) {
		return postService.getPostsByUser(user.getId())
			.stream()
			.limit(DEFAULT_PREVIEW_POST_COUNT)
			.map(post -> new PostSummeryDto(
				post.getId(),
				post.getTitle()
			))
			.toList();
	}
}
