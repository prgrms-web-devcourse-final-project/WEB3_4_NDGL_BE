package com.ndgl.spotfinder.domain.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;

	@Transactional
	public void createPost(PostCreateRequestDto requestDto) {
		postRepository.save(requestDto.toPost());
	}

	@Transactional
	public void updatePost(Long id, PostUpdateRequestDto requestDto) {
		Post post = postRepository.findById(id).orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);

		postRepository.save(post.updatePost(requestDto));
	}
}
