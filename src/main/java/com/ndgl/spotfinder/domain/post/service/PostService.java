package com.ndgl.spotfinder.domain.post.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.image.service.ImageCleanupService;
import com.ndgl.spotfinder.domain.image.service.ImageService;
import com.ndgl.spotfinder.domain.image.type.ImageUsage;
import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.domain.like.type.TargetType;
import com.ndgl.spotfinder.domain.popular.service.redis.RedisPopularService;
import com.ndgl.spotfinder.domain.post.dto.PostCommonUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostDetailResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostTempResponseDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.type.PostStatus;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
	private final PostRepository postRepository;
	private final ImageService imageService;
	private final UserService userService;
	private final ImageCleanupService imageCleanupService;
	private final LikeService likeService;
	private final RedisPopularService redisPopularService;

	private static final int FIRST_PAGE_NUMBER = 0;
	private static final Long DEFAULT_LAST_ID = 0L;

	@Transactional
	public void createPost(PostCreateRequestDto requestDto, String email) {
		User user = userService.findUserByEmail(email);

		Post post = requestDto.toPost(user);
		postRepository.save(post);

		cleanupImages(post);
	}

	@Transactional
	public PostTempResponseDto findOrCreateTempPost(String email) {
		User user = userService.findUserByEmail(email);

		Post post = postRepository.findFirstByUserAndStatus(user, PostStatus.TEMP)
			.orElseGet(() -> {
				Post newPost = Post.createTempPost(user);
				return postRepository.save(newPost);
			});

		return PostTempResponseDto.from(post);
	}

	@Transactional
	public void updatePost(Long id, PostCommonUpdateRequestDto requestDto, String email, PostStatus postStatus) {
		Post post = findPostById(id);

		checkUserPermission(post, email);
		postRepository.save(requestDto.toUpdatedPost(post, postStatus));
		cleanupImages(post);
	}

	@Transactional
	public void softDeletePost(Long id, String email) {
		Post post = findPostById(id);
		checkUserPermission(post, email);
		post.setStatus(PostStatus.DELETED);
		post.setDeleteScheduledAt(LocalDate.now().plusWeeks(1));
	}

	@Transactional
	public void deletePost(Long id) {
		Post post = findPostById(id);
		postRepository.delete(post);
		imageService.deletePostWithAllImages(ImageUsage.POST, post.getId());
		likeService.deleteAllLikes(id, TargetType.POST);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPosts(SliceRequest sliceRequest) {
		PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		Slice<Post> results = postRepository.findByStatusAndIdLessThanOrderByCreatedAtDesc(
			PostStatus.PUBLIC, lastId, pageRequest);
		return convertToSliceResponse(results);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByUser(SliceRequest sliceRequest, Long userId) {
		PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserById(userId);

		Slice<Post> results = postRepository.findByStatusAndUserAndIdLessThanOrderByCreatedAtDesc(
			PostStatus.PUBLIC, user, lastId, pageRequest
		);

		return convertToSliceResponse(results);
	}

	@Transactional(readOnly = true)
	public List<Post> getPostsByUser(Long userId) {
		User user = userService.findUserById(userId);

		return postRepository.findByUser(user);
	}

	@Transactional(readOnly = true)
	public PostDetailResponseDto getPost(String email, Long postId) {
		Post post = findPostById(postId);
		Boolean isLiked = Optional.ofNullable(email)
			.map(userService::findUserByEmail)
			.map(loginUser -> likeService.getLikeStatus(loginUser.getId(), postId, TargetType.POST))
			.orElse(false);

		return new PostDetailResponseDto(post, isLiked);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByLike(SliceRequest sliceRequest, String email) {
		PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserByEmail(email);

		Slice<Post> results = postRepository.findLikedPostsByUser(
			user.getId(), lastId, PostStatus.PUBLIC, pageRequest);

		return convertToSliceResponse(results);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByFollow(SliceRequest sliceRequest, String email) {
		PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserByEmail(email);

		Slice<Post> results = postRepository.findFollowedPostsByUser(
			user.getId(), lastId, PostStatus.PUBLIC, pageRequest
		);

		return convertToSliceResponse(results);
	}

	@Transactional(readOnly = true)
	public Post findPostById(Long id) {
		return postRepository.findById(id)
			.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);
	}

	@Transactional(readOnly = true)
	public Long getLastPostId(SliceRequest sliceRequest) {
		if (sliceRequest.lastId() == null) {
			return postRepository.findTopByOrderByIdDesc()
				.map(post -> post.getId() + 1)
				.orElse(DEFAULT_LAST_ID);
		} else {
			return sliceRequest.lastId();
		}
	}

	@Transactional
	public void incrementPostViewCount(Long postId, Long viewCount) {
		postRepository.incrementViewCount(postId, viewCount);
	}

	private void checkUserPermission(Post post, String email) {
		if (!post.getUser().getEmail().equals(email)) {
			ErrorCode.POST_ACCESS_DENIED.throwServiceException();
		}
	}

	private void cleanupImages(Post post) {
		Set<String> usedImageUrls = extractImageUrlsFromContent(post.getContent());
		imageCleanupService.cleanupUnusedImages(ImageUsage.POST, post.getId(), usedImageUrls, post.getThumbnail());
	}

	private SliceResponse<PostResponseDto> convertToSliceResponse(Slice<Post> results) {
		return new SliceResponse<>(
			results.map(PostResponseDto::new).toList(),
			results.hasNext()
		);
	}

	public Set<String> extractImageUrlsFromContent(String content) {
		Set<String> urls = new HashSet<>();
		Matcher markdownMatcher = Pattern.compile("!\\[\\]\\((https?://[^\\)]+)\\)").matcher(content);
		while (markdownMatcher.find()) {
			urls.add(markdownMatcher.group(1));
		}

		Matcher htmlMatcher = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>").matcher(content);
		while (htmlMatcher.find()) {
			urls.add(htmlMatcher.group(1));
		}
		return urls;
	}

	public List<PostResponseDto> getPopularPosts() {
		return redisPopularService.getPopularPosts();
	}
}
