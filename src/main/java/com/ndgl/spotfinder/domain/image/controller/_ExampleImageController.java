package com.ndgl.spotfinder.domain.image.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ndgl.spotfinder.domain.image.dto.PostRequest;
import com.ndgl.spotfinder.domain.image.dto.PresignedImageResponse;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponse;
import com.ndgl.spotfinder.domain.image.service.ImageService;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.global.rsdata.RsData;

import lombok.RequiredArgsConstructor;

// TODO
// 이 파일은 절대로 기능 구현이 아니며, Image 작동 참고를 위한 예시 컨트롤러다. 추후 삭제할 것!
@Controller
@RequestMapping("/example/images")
@RequiredArgsConstructor
@Profile("!test")
public class _ExampleImageController {

	private final ImageService imageService;
	private final PostRepository postRepository;

	/**
	 * 이미지 업로드 예시 페이지
	 */
	@GetMapping("/upload")
	public String showUploadPage() {
		return "example-upload";
	}

	/**
	 * 이미지 조회 예시 페이지
	 */
	@GetMapping("/view/{postId}")
	public String showImagesPage(@PathVariable long postId, Model model) {
		List<PresignedImageResponse> images = imageService.findImagesWithPresignedUrls(postId);

		model.addAttribute("postId", postId);
		model.addAttribute("images", images);

		return "example-view";
	}

	/**
	 * Presigned URL 발급 API
	 */
	@PostMapping("/presigned-urls")
	@ResponseBody
	public RsData<PresignedUrlsResponse> getPresignedUrls(@RequestBody PostRequest postRequest) {
		PresignedUrlsResponse response = imageService.createImage(postRequest);
		return RsData.success(HttpStatus.OK, response);
	}

	/**
	 * 이미지 업로드 완료 알림 API - 포스트 ID 고정
	 */
	@PostMapping("/upload-complete")
	@ResponseBody
	public RsData<String> completeUpload(@RequestBody UploadCompleteRequest request) {
		// 데모용으로 포스트 ID를 항상 1로 고정
		long fixedPostId = 1L;

		// 고정된 포스트 ID와 클라이언트에서 받은 이미지 URL을 저장
		imageService.saveImages(fixedPostId, request.imageUrls());

		return RsData.success(HttpStatus.OK);
	}

	/**
	 * 이미지 업로드 완료 요청 DTO
	 */
	public record UploadCompleteRequest(
		List<String> imageUrls
	) {
	}

	/**
	 * 이미지 삭제 API
	 *
	 * @param imageId 삭제할 이미지 ID
	 * @return 삭제 결과
	 */
	@DeleteMapping("/delete/{imageId}")
	@ResponseBody
	public RsData<String> deleteImage(@PathVariable Long imageId) {
		try {
			imageService.deleteImage(imageId);
			return RsData.success(HttpStatus.OK);
		} catch (Exception e) {
			return RsData.success(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 포스트 존재 확인용 테스트 API
	 */
	@GetMapping("/test-post")
	@ResponseBody
	public String testPostExists() {
		try {
			boolean exists = postRepository.existsById(1L);
			return "포스트 ID 1 존재 여부: " + exists;
		} catch (Exception e) {
			return "오류 발생: " + e.getMessage();
		}
	}

} 