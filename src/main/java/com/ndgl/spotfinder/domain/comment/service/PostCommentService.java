package com.ndgl.spotfinder.domain.comment.service;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentRequestDto;
import com.ndgl.spotfinder.domain.comment.dto.PostCommentResponseDto;
import com.ndgl.spotfinder.domain.comment.entity.PostComment;
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
		Boolean isLiked = Optional.ofNullable(email)
			.map(userService::findUserByEmail)
			.map(loginUser -> likeService.getLikeStatus(loginUser.getId(), commentId, Like.TargetType.COMMENT))
			.orElse(false);

		return new PostCommentResponseDto(comment, isLiked);
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
		postCommentRepository.delete(comment);
		likeService.deleteAllLikes(commentId, Like.TargetType.COMMENT);
	}

	@Transactional
	public void modify(Long postId, Long commentId, String content, String email) {
		User author = userService.findUserByEmail(email);

		PostComment comment = findCommentAndVerifyPost(commentId, postId);
		comment.checkAuthorCanModify(author);
		comment.setContent(content);
	}

	private SliceResponse<PostCommentResponseDto> convertToSliceResponse(long userId, Slice<PostComment> results) {
		return new SliceResponse<>(
			results.map(comment ->
				new PostCommentResponseDto(comment,
					likeService.getLikeStatus(userId, comment.getId(), Like.TargetType.COMMENT)
				)).toList(),
			results.hasNext()
		);
	}

	private SliceResponse<PostCommentResponseDto> convertToSliceResponse(Slice<PostComment> results) {
		return new SliceResponse<>(
			results.map(comment -> new PostCommentResponseDto(comment, false)).toList(),
			results.hasNext()
		);
	}
}
