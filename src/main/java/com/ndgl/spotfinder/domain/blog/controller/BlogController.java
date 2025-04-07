package com.ndgl.spotfinder.domain.blog.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.blog.dto.BlogResponseDto;
import com.ndgl.spotfinder.domain.blog.service.BlogService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/blogs")
public class BlogController {
	private final BlogService blogService;

	@GetMapping()
	public SliceResponse<BlogResponseDto> getAllBlogs(
		@ModelAttribute @Valid SliceRequest sliceRequest,
		Principal principal
	) {
		return blogService.getBlogs(sliceRequest, principal.getName());
	}
}
