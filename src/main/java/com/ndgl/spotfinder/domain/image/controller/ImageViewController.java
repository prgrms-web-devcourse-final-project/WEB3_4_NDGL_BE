package com.ndgl.spotfinder.domain.image.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ndgl.spotfinder.domain.image.type.ImageType;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/view-example/images")
@RequiredArgsConstructor
@Profile("dev")
public class ImageViewController {

	/**
	 * 새 포스트 생성 페이지 - 실제로 연동되는 업로드 예시
	 */
	@GetMapping("/post-upload")
	public String showPostWithUpload(Model model) {
		model.addAttribute("id", 999L);  // 신규 포스트는 임시 ID 사용
		model.addAttribute("imageType", ImageType.POST);
		model.addAttribute("pageTitle", "새 포스트 작성");
		model.addAttribute("maxImageCount", 10);
		model.addAttribute("allowedTypes", "image/jpeg, image/png, image/gif, image/webp");
		model.addAttribute("maxFileSize", 5);

		return "post-with-upload";  // 새로운 템플릿 사용 (아래에서 생성)
	}
}