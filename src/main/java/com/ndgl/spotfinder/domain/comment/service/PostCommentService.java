package com.ndgl.spotfinder.domain.comment.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentRequestDto;
import com.ndgl.spotfinder.domain.comment.dto.PostCommentResponseDto;
import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.entity.PostCommentStatus;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostCommentService {
	private final PostCommentRepository postCommentRepository;
	private final UserService userService;
	private final PostService postService;
	private final LikeService likeService;

	@Transactional(readOnly = true)
	public SliceResponse<PostCommentResponseDto> getComments(String email, Long postId, Long lastId, int size) {
		Pageable pageable = PageRequest.of(0, size);
		long startId = (lastId != null) ? lastId : Long.MAX_VALUE;

		Slice<PostComment> comments = postCommentRepository
			.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, startId, pageable);

		return Optional.ofNullable(email)
			.map(userService::findUserByEmail)
			.map(loginUser -> convertToSliceResponse(loginUser.getId(), comments))
			.orElseGet(() -> convertToSliceResponse(comments));
	}

	private PostComment findCommentAndVerifyPost(Long commentId, Long postId) {
		PostComment comment = findCommentById(commentId);
		comment.isCommentOfPost(postId);
		return comment;
	}

	@Transactional(readOnly = true)
	public PostComment findCommentById(Long id) {
		return postCommentRepository.findById(id)
			.orElseThrow(ErrorCode.COMMENT_NOT_FOUND::throwServiceException);
	}

	@Transactional(readOnly = true)
	public PostCommentResponseDto getComment(String email, Long postId, Long commentId) {
		PostComment comment = findCommentAndVerifyPost(commentId, postId);
		return new PostCommentResponseDto(comment);
	}

	@Transactional
	public void write(Long postId, PostCommentRequestDto reqBody, String email) {
		String content = reqBody.content();
		Long parentId = reqBody.parentId();

		Post post = postService.findPostById(postId);
		User user = userService.findUserByEmail(email);

		PostComment.PostCommentBuilder commentBuilder = PostComment.builder()
			.user(user)
			.post(post)
			.content(content)
			.likeCount(0L);

		if (parentId != null) { // 대댓글 여부
			PostComment parentComment = findCommentById(parentId);
			commentBuilder.parentComment(parentComment);
		}

		postCommentRepository.save(commentBuilder.build());
	}

	@Transactional
	public void delete(Long id, Long commentId, String email) {
		User author = userService.findUserByEmail(email);

		PostComment comment = findCommentAndVerifyPost(commentId, id);
		comment.checkAuthorCanDelete(author);

		comment.setPinned(false); // 댓글 고정 해제
		comment.setStatus(PostCommentStatus.DELETED);
		likeService.deleteAllLikes(commentId, Like.TargetType.COMMENT);
	}

	@Transactional
	public void modify(Long postId, Long commentId, String content, String email) {
		User author = userService.findUserByEmail(email);

		PostComment comment = findCommentAndVerifyPost(commentId, postId);
		comment.checkAuthorCanModify(author);
		comment.setContent(content);
	}

	// 로그인 사용자
	private SliceResponse<PostCommentResponseDto> convertToSliceResponse(long userId, Slice<PostComment> results) {
		List<Long> allCommentIds = collectAllCommentIds(results.getContent());
		Map<Long, Boolean> likeStatusMap = likeService.getAllLikeStatus(
			userId, allCommentIds, Like.TargetType.COMMENT);

		return new SliceResponse<>(
			results.map(comment -> createResponseWithLikeStatusMap(comment, likeStatusMap)).toList(),
			results.hasNext()
		);
	}

	// 비 로그인 사용자
	private SliceResponse<PostCommentResponseDto> convertToSliceResponse(Slice<PostComment> results) {
		return new SliceResponse<>(
			results.map(comment -> new PostCommentResponseDto(comment, false))
				.toList(),
			results.hasNext()
		);
	}

	private List<Long> collectAllCommentIds(List<PostComment> comments) {
		List<Long> allIds = new ArrayList<>();

		for (PostComment comment : comments) {
			allIds.add(comment.getId());

			if (comment.getChildrenComments() != null) {
				comment.getChildrenComments().forEach(child -> allIds.add(child.getId()));
			}
		}

		return allIds;
	}

	private PostCommentResponseDto createResponseWithLikeStatusMap(
		PostComment comment,
		Map<Long, Boolean> likeStatusMap
	) {
		Boolean isLiked = likeStatusMap.getOrDefault(comment.getId(), false);

		if (comment.getChildrenComments() == null) {
			return new PostCommentResponseDto(comment, isLiked);
		}

		List<PostCommentResponseDto> childrenComments = comment.getChildrenComments().stream()
			.sorted(Comparator.comparing(PostComment::getId).reversed())
			.map(child -> new PostCommentResponseDto(
				child,
				likeStatusMap.getOrDefault(child.getId(), false)
			))
			.toList();

		return new PostCommentResponseDto(comment, isLiked, childrenComments);
	}

	@Transactional
	public void pinComment(Long postId, Long commentId, String email) {
		Post post = postService.findPostById(postId);
		User user = userService.findUserByEmail(email);

		// 고정 권한 체크 (작성자만 가능)
		if (!post.getUser().getId().equals(user.getId())) {
			ErrorCode.COMMENT_PIN_DENIED.throwServiceException();
		}

		PostComment targetComment = findCommentById(commentId);
		targetComment.isCommentOfPost(postId);

		// 기존 고정 댓글 해제
		Optional<PostComment> existingPinnedCommentOpt = postCommentRepository.findPinnedCommentByPostId(postId);
		if (existingPinnedCommentOpt.isPresent()) {
			PostComment existingPinnedComment = existingPinnedCommentOpt.get();

			// 이미 고정된 댓글을 다시 요청한 경우 → 고정 해제
			if (existingPinnedComment.getId().equals(commentId)) {
				existingPinnedComment.setPinned(false);
				return;
			}

			// 다른 댓글이 고정되어 있는 경우 → 기존 해제 후 대상 고정
			existingPinnedComment.setPinned(false);
		}

		targetComment.setPinned(true);
	}
}
