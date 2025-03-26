package com.ndgl.spotfinder.domain.post.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {
	private final PostService postService;

	@PostMapping
	public RsData<String> createPost(@RequestBody PostCreateRequestDto postCreateRequestDto) {
		postService.createPost(postCreateRequestDto);

		return RsData.success(HttpStatus.OK);
	}
}
