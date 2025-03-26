package com.ndgl.spotfinder.domain.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;

	@Transactional
	public void createPost(PostCreateRequestDto postCreateRequestDto) {
		postRepository.save(postCreateRequestDto.toPost());
	}
}
