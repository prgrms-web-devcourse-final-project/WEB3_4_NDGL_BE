package com.ndgl.spotfinder.domain.like.service;

import static com.ndgl.spotfinder.domain.like.entity.Like.*;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.service.PostCommentService;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final UserService userService;
	private final PostService postService;
	private final PostCommentService postCommentService;

	/**
	 * 좋아요 추가 또는 삭제
	 *
	 * @param userId     유저 ID
	 * @param targetId   타겟 ID
	 * @param targetType 타겟 타입
	 * @return 토글 결과 (true: 좋아요 추가, false : 좋아요 취소)
	 */
	private boolean toggleLike(long userId, long targetId, TargetType targetType) {
		validateTargetId(targetId);

		Optional<Like> existingLike = likeRepository.findByUserIdAndTargetIdAndTargetType(
			userId, targetId, targetType
		);

		if (existingLike.isPresent()) {
			deleteLike(existingLike.get(), targetId, targetType);
			return false;
		} else {
			createLike(userId, targetId, targetType);
			return true;
		}
	}

	private void validateTargetId(long targetId) {
		if (targetId <= 0) {
			ErrorCode.UNSUPPORTED_TARGET_TYPE.throwServiceException();
		}
	}

	/**
	 * 포스트 좋아요 토글
	 */
	@Transactional
	public boolean togglePostLike(long userId, long postId) {
		return toggleLike(userId, postId, TargetType.POST);
	}

	/**
	 * 댓글 좋아요 토글
	 */
	@Transactional
	public boolean toggleCommentLike(long userId, long commentId) {
		return toggleLike(userId, commentId, TargetType.COMMENT);
	}

	/**
	 * 포스트 좋아요 수 조회
	 */
	@Transactional(readOnly = true)
	public Long getPostLikeCount(long postId) {
		return getLikeCount(postId, TargetType.POST);
	}

	/**
	 * 댓글 좋아요 수 조회
	 */
	@Transactional(readOnly = true)
	public Long getCommentLikeCount(long commentId) {
		return getLikeCount(commentId, TargetType.COMMENT);
	}

	/**
	 * 현재 사용자의 포스트 좋아요 상태 조회
	 *
	 * @return 좋아요 했다면 true, 아니면 false
	 */
	@Transactional(readOnly = true)
	public Boolean getPostLikeStatus(long userId, long postId) {
		return getLikeStatus(userId, postId, TargetType.POST);
	}

	/**
	 * 현재 사용자의 댓글 좋아요 상태 조회
	 *
	 * @return 좋아요 했다면 true, 아니면 false
	 */
	@Transactional(readOnly = true)
	public Boolean getCommentLikeStatus(long userId, long commentId) {
		return getLikeStatus(userId, commentId, TargetType.COMMENT);
	}

	/**
	 * 포스트 좋아요 모두 삭제
	 */
	@Transactional
	public void deleteAllLikesForPost(Long postId) {
		deleteAllLikes(postId, TargetType.POST);
	}

	/**
	 * 댓글 좋아요 모두 삭제
	 */
	@Transactional
	public void deleteAllLikesForComment(Long commentId) {
		deleteAllLikes(commentId, TargetType.COMMENT);
	}

	/**
	 * 대상의 좋아요 수 조회
	 */
	private Long getLikeCount(long targetId, TargetType targetType) {
		validateTargetId(targetId);
		return likeRepository.countByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 대상의 좋아요 상태 조회
	 */
	private Boolean getLikeStatus(long userId, long targetId, TargetType targetType) {
		validateTargetId(targetId);
		return likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
	}

	/**
	 * 대상의 모든 좋아요 삭제
	 */
	private void deleteAllLikes(long targetId, TargetType targetType) {
		likeRepository.deleteByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 좋아요 삭제 및 대상 좋아요 카운트 감소
	 */
	private void deleteLike(Like like, long targetId, TargetType targetType) {
		updateTargetLikeCount(targetId, targetType, -1);
		likeRepository.delete(like);
	}

	/**
	 * 좋아요 생성 및 대상 좋아요 카운트 증가
	 */
	private void createLike(long userId, long targetId, TargetType targetType) {
		User user = userService.findUserById(userId);
		Like like = builder()
			.user(user)
			.targetId(targetId)
			.targetType(targetType)
			.build();

		updateTargetLikeCount(targetId, targetType, 1);
		likeRepository.save(like);
	}

	/**
	 * 대상(게시물/댓글)의 좋아요 수 업데이트
	 */
	private void updateTargetLikeCount(long targetId, TargetType targetType, int num) {
		switch (targetType) {
			case POST -> {
				Post post = postService.findPostById(targetId);
				post.updateLikeCount(post.getLikeCount() + num);
			}
			case COMMENT -> {
				PostComment comment = postCommentService.findById(targetId);
				comment.updateLikeCount(comment.getLikeCount() + num);
			}
			default -> ErrorCode.UNSUPPORTED_TARGET_TYPE.throwServiceException();
		}
	}

}
