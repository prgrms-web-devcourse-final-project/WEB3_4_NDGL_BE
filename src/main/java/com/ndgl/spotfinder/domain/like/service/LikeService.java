package com.ndgl.spotfinder.domain.like.service;

import static com.ndgl.spotfinder.domain.like.entity.Like.*;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

	private final LikeRepository likeRepository;
	private final UserRepository userRepository;

	/**
	 * 좋아요 추가 또는 삭제
	 *
	 * @param userId     유저 ID
	 * @param targetId   타겟 ID
	 * @param targetType 타겟 타입
	 * @return 토글 결과 (true: 좋아요 추가, false : 좋아요 취소)
	 */
	@Transactional
	public boolean toggleLike(long userId, long targetId, TargetType targetType) {
		validateTargetId(targetId);

		Optional<Like> existingLike = likeRepository.findByUserIdAndTargetIdAndTargetType(
			userId, targetId, targetType
		);

		if (existingLike.isPresent()) { // 이미 있다면, 좋아요 취소
			likeRepository.delete(existingLike.get());
			return false;
		} else { // 없다면, 좋아요 추가
			Like like = new Like();
			User user = userRepository.findById(userId)
				.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);

			switch (targetType) {
				case POST -> like = createPostLike(user, targetId);
				case COMMENT -> like = createCommentLike(user, targetId);
				default -> ErrorCode.UNSUPPORTED_TARGET_TYPE.throwServiceException();
			}

			likeRepository.save(like);
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
	public Long getPostLikeCount(long postId) {
		return getLikeCount(postId, TargetType.POST);
	}

	/**
	 * 댓글 좋아요 수 조회
	 */
	public Long getCommentLikeCount(long commentId) {
		return getLikeCount(commentId, TargetType.COMMENT);
	}

	/**
	 * 대상의 좋아요 수 조회
	 */
	private Long getLikeCount(long targetId, TargetType targetType) {
		validateTargetId(targetId);
		return likeRepository.countByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 포스트 좋아요 수 조회
	 */
	public Boolean getPostLikeStatus(long userId, long postId) {
		return getLikeStatus(userId, postId, TargetType.POST);
	}

	/**
	 * 댓글 좋아요 수 조회
	 */
	public Boolean getCommentLikeStatus(long userId, long commentId) {
		return getLikeStatus(userId, commentId, TargetType.COMMENT);
	}

	/**
	 * 대상의 좋아요 상태 조회
	 */
	public Boolean getLikeStatus(long userId, long targetId, TargetType targetType) {
		validateTargetId(targetId);
		return likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
	}

	/**
	 * 포스트 좋아요 삭제
	 */
	public void deleteAllLikesForPost(Long postId) {
		deleteAllLikes(postId, TargetType.POST);
	}

	/**
	 * 댓글 좋아요 삭제
	 */
	public void deleteAllLikesForComment(Long commentId) {
		deleteAllLikes(commentId, TargetType.COMMENT);
	}

	/**
	 * 대상의 모든 좋아요 삭제
	 */
	@Transactional
	public void deleteAllLikes(long targetId, TargetType targetType) {
		likeRepository.deleteByTargetIdAndTargetType(targetId, targetType);
	}
}
