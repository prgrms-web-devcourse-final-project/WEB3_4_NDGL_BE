package com.ndgl.spotfinder.domain.post.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostDetailResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final UserService userService;

	private static final Long DEFAULT_LAST_ID = 0L;

	@Transactional
	public void createPost(PostCreateRequestDto requestDto, String email) {
		User user = userService.findUserByEmail(email);

		postRepository.save(requestDto.toPost(user));
	}

	@Transactional
	public void updatePost(Long id, PostUpdateRequestDto requestDto, String email) {
		Post post = findPostById(id);

		checkUserPermission(post, email);
		postRepository.save(requestDto.toUpdatedPost(post));
	}

	@Transactional
	public void deletePost(Long id, String email) {
		Post post = findPostById(id);

		checkUserPermission(post, email);
		postRepository.delete(post);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPosts(SliceRequest sliceRequest) {
		PageRequest pageRequest = PageRequest.of(0, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);

		Slice<Post> results = postRepository.findByIdLessThanOrderByCreatedAtDesc(lastId, pageRequest);

		return convertToSliceResponse(results);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByUser(SliceRequest sliceRequest, Long userId) {
		PageRequest pageRequest = PageRequest.of(0, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserById(userId);

		Slice<Post> results = postRepository.findByUserAndIdLessThanOrderByCreatedAtDesc(user, lastId, pageRequest);

		return convertToSliceResponse(results);
	}

	@Transactional(readOnly = true)
	public List<Post> getPostsByUser(Long userId) {
		User user = userService.findUserById(userId);

		return postRepository.findByUser(user);
	}

	@Transactional(readOnly = true)
	public PostDetailResponseDto getPost(Long id) {
		Post post = findPostById(id);

		return new PostDetailResponseDto(post);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByLike(SliceRequest sliceRequest, String email) {
		PageRequest pageRequest = PageRequest.of(0, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserByEmail(email);

		Slice<Post> results = postRepository.findLikedPostsByUser(user.getId(), lastId, pageRequest);

		return convertToSliceResponse(results);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByFollow(SliceRequest sliceRequest, String email) {
		PageRequest pageRequest = PageRequest.of(0, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserByEmail(email);

		Slice<Post> results = postRepository.findFollowedPostsByUser(user.getId(), lastId, pageRequest);

		return convertToSliceResponse(results);
	}

	@Transactional(readOnly = true)
	public Post findPostById(Long id) {
		return postRepository.findById(id)
			.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);
	}

	@Transactional(readOnly = true)
	public Long getLastPostId(SliceRequest sliceRequest) {
		if (sliceRequest.lastId() == null) {
			return postRepository.findTopByOrderByIdDesc()
				.map(post -> post.getId() + 1)
				.orElse(DEFAULT_LAST_ID);
		} else {
			return sliceRequest.lastId();
		}
	}

	private void checkUserPermission(Post post, String email) {
		if (!post.getUser().getEmail().equals(email)) {
			ErrorCode.POST_ACCESS_DENIED.throwServiceException();
		}
	}

	private SliceResponse<PostResponseDto> convertToSliceResponse(Slice<Post> results) {
		return new SliceResponse<>(
			results.map(PostResponseDto::new).toList(),
			results.hasNext()
		);
	}
}
