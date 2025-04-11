package com.ndgl.spotfinder.domain.post.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.image.service.ImageCleanupService;
import com.ndgl.spotfinder.domain.image.service.ImageService;
import com.ndgl.spotfinder.domain.image.type.ImageUsage;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.domain.post.dto.PostCommonUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostDetailResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostTempResponseDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.entity.PostStatus;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
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
	public void updatePost(Long id, PostCommonUpdateRequestDto requestDto, String email, boolean temp) {
		Post post = findPostById(id);

		checkUserPermission(post, email);
		postRepository.save(requestDto.toUpdatedPost(post, temp));

		cleanupImages(post);
	}

	@Transactional
	public void deletePost(Long id, String email) {
		Post post = findPostById(id);
		checkUserPermission(post, email);
		postRepository.delete(post);
		imageService.deletePostWithAllImages(ImageUsage.POST, post.getId());
		likeService.deleteAllLikes(id, Like.TargetType.POST);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPosts(String email, SliceRequest sliceRequest) {
		PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		Slice<Post> results = postRepository.findByIdLessThanOrderByCreatedAtDesc(lastId, pageRequest);

		return Optional.ofNullable(email)
			.map(userService::findUserByEmail)
			.map(loginUser -> convertToSliceResponse(loginUser.getId(), results))
			.orElseGet(() -> convertToSliceResponse(null, results));
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByUser(SliceRequest sliceRequest, Long userId, String email) {
		PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User user = userService.findUserById(userId);

		Slice<Post> results = postRepository.findByUserAndIdLessThanOrderByCreatedAtDesc(user, lastId, pageRequest);
		User loginUser = userService.findUserByEmail(email);

		return convertToSliceResponse(loginUser.getId(), results);
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
			.map(loginUser -> likeService.getLikeStatus(loginUser.getId(), postId, Like.TargetType.POST))
			.orElse(false);

		return new PostDetailResponseDto(post, isLiked);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByLike(SliceRequest sliceRequest, String email) {
		PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User loginUser = userService.findUserByEmail(email);

		Slice<Post> results = postRepository.findLikedPostsByUser(loginUser.getId(), lastId, pageRequest);

		return convertToSliceResponse(loginUser.getId(), results);
	}

	@Transactional(readOnly = true)
	public SliceResponse<PostResponseDto> getPostsByFollow(SliceRequest sliceRequest, String email) {
		PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, sliceRequest.size());
		Long lastId = getLastPostId(sliceRequest);
		User loginUser = userService.findUserByEmail(email);

		Slice<Post> results = postRepository.findFollowedPostsByUser(loginUser.getId(), lastId, pageRequest);

		return convertToSliceResponse(loginUser.getId(), results);
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

	private void checkUserPermission(Post post, String email) {
		if (!post.getUser().getEmail().equals(email)) {
			ErrorCode.POST_ACCESS_DENIED.throwServiceException();
		}
	}

	private void cleanupImages(Post post) {
		Set<String> usedImageUrls = extractImageUrlsFromContent(post.getContent());
		imageCleanupService.cleanupUnusedImages(ImageUsage.POST, post.getId(), usedImageUrls);
	}

	private SliceResponse<PostResponseDto> convertToSliceResponse(Long userId, Slice<Post> results) {
		List<Post> posts = results.getContent();
		Map<Long, Boolean> likeStatusMap = getLikeStatusMap(userId, posts);
		List<PostResponseDto> responseDtos = createPostResponseDtos(posts, likeStatusMap);
		return new SliceResponse<>(responseDtos, results.hasNext());
	}

	private Map<Long, Boolean> getLikeStatusMap(Long userId, List<Post> posts) {
		if (userId == null || posts.isEmpty()) {
			return Collections.emptyMap();
		}
		
		List<Long> postIds = posts.stream()
			.map(Post::getId)
			.collect(Collectors.toList());
		return likeService.getAllLikeStatus(userId, postIds, Like.TargetType.POST);
	}

	private List<PostResponseDto> createPostResponseDtos(List<Post> posts, Map<Long, Boolean> likeStatusMap) {
		return posts.stream()
			.map(post -> new PostResponseDto(
				post,
				likeStatusMap.getOrDefault(post.getId(), false)
			))
			.collect(Collectors.toList());
	}

	public Set<String> extractImageUrlsFromContent(String content) {
		Set<String> urls = new HashSet<>();
		Matcher markdownMatcher = Pattern.compile("!\\[\\]\\((https?://[^\\)]+)\\)").matcher(content);
		while (markdownMatcher.find()) {
			urls.add(markdownMatcher.group(1));
		}
		return urls;
	}
}
