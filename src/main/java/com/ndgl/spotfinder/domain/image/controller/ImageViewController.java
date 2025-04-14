package com.ndgl.spotfinder.domain.image.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ndgl.spotfinder.domain.image.type.ImageUsage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/view-example/images")
@RequiredArgsConstructor
@Profile("dev")
public class ImageViewController {

	/**
	 * 글 작성 페이지 목록 - 글쓰기 버튼이 포함된 인덱스 페이지
	 */
	@GetMapping
	public String showImageIndex() {
		return "image-index";
	}

	/**
	 * 새 포스트 생성 페이지 - 임시글 생성 후 업로드 예시
	 */
	@GetMapping("/post-upload")
	public String showPostWithUpload(Model model) {
		// User testUser = userService.findUserByEmail(TEST_USER_EMAIL);
		//
		// PostTempResponse tempPost = postService.findOrCreateTempPost(testUser.getEmail());
		//
		// model.addAttribute("id", tempPost.id());
		// model.addAttribute("title", tempPost.title());
		// model.addAttribute("content", tempPost.content());

		model.addAttribute("imageType", ImageUsage.POST);
		model.addAttribute("pageTitle", "새 포스트 작성");
		model.addAttribute("maxImageCount", 10);
		model.addAttribute("allowedTypes", "image/jpeg, image/png, image/gif, image/webp");
		model.addAttribute("maxFileSize", 5);

		return "post-with-upload";
	}
}