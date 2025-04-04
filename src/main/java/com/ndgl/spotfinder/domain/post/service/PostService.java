package com.ndgl.spotfinder.domain.post.service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.image.service.ImageCleanupService;
import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostDetailResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final UserService userService;
	private final ImageCleanupService imageCleanupService;

	@Transactional
	public void createPost(PostCreateRequestDto requestDto, String email) {
		User user = userService.findUserByEmail(email);

		Post post = requestDto.toPost(user);
		postRepository.save(post);
		
		Set<String> usedImageUrls = extractImageUrlsFromContent(post.getContent());
		imageCleanupService.cleanupUnusedImages(ImageType.POST, post.getId(), usedImageUrls);
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

	public SliceResponse<PostResponseDto> getPosts(SliceRequest sliceRequest) {
		PageRequest pageRequest = PageRequest.of(0, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);

		Slice<Post> results = postRepository.findByIdLessThanOrderByCreatedAtDesc(lastId, pageRequest);

		return convertToSliceResponse(results);
	}

	public SliceResponse<PostResponseDto> getPostsByUser(SliceRequest sliceRequest, Long userId) {
		PageRequest pageRequest = PageRequest.of(0, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserById(userId);

		Slice<Post> results = postRepository.findByUserAndIdLessThanOrderByCreatedAtDesc(user, lastId, pageRequest);

		return convertToSliceResponse(results);
	}

	public PostDetailResponseDto getPost(Long id) {
		Post post = findPostById(id);

		return new PostDetailResponseDto(post);	}

	public SliceResponse<PostResponseDto> getPostsByLike(SliceRequest sliceRequest, String email) {
		PageRequest pageRequest = PageRequest.of(0, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserByEmail(email);

		Slice<Post> results = postRepository.findLikedPostsByUser(user.getId(), lastId, pageRequest);

		return convertToSliceResponse(results);
	}

	public Post findPostById(Long id) {
		return postRepository.findById(id)
			.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);
	}

	private void checkUserPermission(Post post, String email) {
		if (!post.getUser().getEmail().equals(email)) {
			ErrorCode.POST_ACCESS_DENIED.throwServiceException();
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

	private SliceResponse<PostResponseDto> convertToSliceResponse(Slice<Post> results) {
		return new SliceResponse<>(
			results.map(PostResponseDto::new).toList(),
			results.hasNext()
		);
	}

	/**
	 * 컨텐츠에서 이미지 URL을 추출하는 헬퍼 메서드
	 */
	private Set<String> extractImageUrlsFromContent(String content) {
		Set<String> urls = new HashSet<>();				
				
		Pattern markdownPattern = Pattern.compile("!\\[\\]\\((https?://[^\\)]+)\\)");
		Matcher markdownMatcher = markdownPattern.matcher(content);
		while (markdownMatcher.find()) {
			urls.add(markdownMatcher.group(1));
		}
		
		return urls;
	}
}
