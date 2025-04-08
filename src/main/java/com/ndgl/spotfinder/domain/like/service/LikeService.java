package com.ndgl.spotfinder.domain.like.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.service.PostCommentService;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
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
	 */
	@Transactional
	public boolean toggleLike(long userId, long targetId, TargetType targetType) {
		validateTargetId(targetId);

		return likeRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)
			.map(like -> {
				deleteLike(like, targetId, targetType);
				return false;
			})
			.orElseGet(() -> {
				createLike(userId, targetId, targetType);
				return true;
			});
	}

	/**
	 * 타겟 ID의 유효성을 검증
	 */
	private void validateTargetId(long targetId) {
		if (targetId <= 0) {
			ErrorCode.UNSUPPORTED_TARGET_TYPE.throwServiceException();
		}
	}

	/**
	 * 대상의 좋아요 수 조회
	 */
	@Transactional(readOnly = true)
	public Long getLikeCount(long targetId, TargetType targetType) {
		validateTargetId(targetId);
		return likeRepository.countByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 사용자가 특정 대상에 좋아요를 눌렀는지 상태 조회
	 */
	@Transactional(readOnly = true)
	public Boolean getLikeStatus(long userId, long targetId, TargetType targetType) {
		validateTargetId(targetId);
		return likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
	}

	/**
	 * 대상의 모든 좋아요 삭제 (대상이 삭제될 때 호출)
	 */
	@Transactional
	public void deleteAllLikes(long targetId, TargetType targetType) {
		validateTargetId(targetId);
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
		Like like = Like.builder()
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
	private void updateTargetLikeCount(long targetId, TargetType targetType, int delta) {
		switch (targetType) {
			case POST -> {
				Post post = postService.findPostById(targetId);
				post.updateLikeCount(delta);
			}
			case COMMENT -> {
				PostComment comment = postCommentService.findById(targetId);
				comment.updateLikeCount(delta);
			}
			default -> ErrorCode.UNSUPPORTED_TARGET_TYPE.throwServiceException();
		}
	}
}
