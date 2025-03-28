package com.ndgl.spotfinder.domain.image.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ndgl.spotfinder.domain.image.dto.PresignedImageResponse;
import com.ndgl.spotfinder.domain.image.service.ImageService;
import com.ndgl.spotfinder.domain.image.type.ImageType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// TODO
// 이 파일은 절대로 기능 구현이 아니며, Image 작동 참고를 위한 예시 컨트롤러다. 추후 삭제할 것!
@Controller
@RequestMapping("/temp")
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class _ImageExampleController {

	private final ImageService imageService;

	@GetMapping("/{postId}/upload")
	public String showUploadPage(@PathVariable long postId, Model model) {
		model.addAttribute("postId", postId);

		List<PresignedImageResponse> imagesResponse = imageService.findImagesWithPresignedUrls(postId);
		model.addAttribute("images", imagesResponse);

		return "upload";
	}

	@PostMapping("/upload/{postId}")
	public String uploadImages(
		@PathVariable long postId,
		@RequestParam("files") List<MultipartFile> files) {

		imageService.uploadAndSaveImages(postId, files, ImageType.POST);
		return "redirect:/temp/" + postId + "/upload";
	}

	@DeleteMapping("images/{imageId}")
	@ResponseBody
	public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
		imageService.deleteImage(imageId);
		return ResponseEntity.ok().build();
	}
}