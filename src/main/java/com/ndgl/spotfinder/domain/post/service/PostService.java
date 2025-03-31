package com.ndgl.spotfinder.domain.post.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	private final UserRepository userRepository;
	private final PostRepository postRepository;

	@Transactional
	public void createPost(PostCreateRequestDto requestDto, String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);

		postRepository.save(requestDto.toPost(user));
	}

	@Transactional
	public void updatePost(Long id, PostUpdateRequestDto requestDto, String email) {
		Post post = findPostById(id);

		checkUserPermission(post, email);
		postRepository.save(post.updatePost(requestDto));
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

		return new SliceResponse<>(
			results.map(PostResponseDto::new).toList(),
			results.hasNext()
		);
	}

	public PostResponseDto getPost(Long id) {
		Post post = findPostById(id);

		return new PostResponseDto(post);
	}

	private Post findPostById(Long id) {
		return postRepository.findById(id)
			.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);
	}

	private void checkUserPermission(Post post, String email) {
		if (!post.getUser().getEmail().equals(email)) {
			ErrorCode.ACCESS_DENIED.throwServiceException();
		}
	}

	private Long getLastPostId(SliceRequest sliceRequest) {
		if (sliceRequest.lastId() == null) {
			return postRepository.findTopByOrderByIdDesc()
				.map(post -> post.getId() + 1)
				.orElse(0L);
		} else {
			return sliceRequest.lastId();
		}
	}
}
