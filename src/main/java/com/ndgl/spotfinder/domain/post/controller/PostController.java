package com.ndgl.spotfinder.domain.post.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostDetailResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostTempResponse;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController implements PostApiSpecification {
	private final PostService postService;

	@PostMapping
	public RsData<Void> createPost(
		@RequestBody @Valid PostCreateRequestDto postCreateRequestDto,
		Principal principal
	) {
		postService.createPost(postCreateRequestDto, principal.getName());

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/temp")
	public RsData<PostTempResponse> createTempPost(Principal principal) {
		PostTempResponse response = postService.findOrCreateTempPost(principal.getName());
		return RsData.success(HttpStatus.CREATED, response);
	}

	@PutMapping("/{id}")
	public RsData<Void> updatePost(
		@PathVariable Long id,
		@RequestBody @Valid PostUpdateRequestDto postUpdateRequestDto,
		Principal principal
	) {
		postService.updatePost(id, postUpdateRequestDto, principal.getName());

		return RsData.success(HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public RsData<Void> deletePost(
		@PathVariable Long id,
		Principal principal
	) {
		postService.deletePost(id, principal.getName());

		return RsData.success(HttpStatus.OK);
	}

	@GetMapping
	public RsData<SliceResponse<PostResponseDto>> getPosts(@ModelAttribute @Valid SliceRequest sliceRequest) {
		SliceResponse<PostResponseDto> results = postService.getPosts(sliceRequest);

		return RsData.success(HttpStatus.OK, results);
	}

	@GetMapping("/{id}")
	public RsData<PostDetailResponseDto> getPost(@PathVariable Long id) {
		PostDetailResponseDto result = postService.getPost(id);

		return RsData.success(HttpStatus.OK, result);
	}

	@GetMapping("/users/{userId}")
	public RsData<SliceResponse<PostResponseDto>> getPostsByUserId(
		@PathVariable Long userId,
		@ModelAttribute @Valid SliceRequest sliceRequest
	) {
		SliceResponse<PostResponseDto> results = postService.getPostsByUser(sliceRequest, userId);

		return RsData.success(HttpStatus.OK, results);
	}

	@GetMapping("/like")
	public RsData<SliceResponse<PostResponseDto>> getPostsByLike(
		@ModelAttribute @Valid SliceRequest sliceRequest,
		Principal principal
	) {
		SliceResponse<PostResponseDto> results = postService.getPostsByLike(sliceRequest, principal.getName());

		return RsData.success(HttpStatus.OK, results);
	}

	@GetMapping("/follow")
	public RsData<SliceResponse<PostResponseDto>> getPostsByFollow(
		@ModelAttribute @Valid SliceRequest sliceRequest,
		Principal principal
	) {
		SliceResponse<PostResponseDto> results = postService.getPostsByFollow(sliceRequest, principal.getName());

		return RsData.success(HttpStatus.OK, results);
	}
}
