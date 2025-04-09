package com.ndgl.spotfinder.domain.like.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final UserService userService;
	private final LikeTargetService likeTargetService;

	/**
	 * 좋아요 추가 또는 삭제
	 *
	 * @return true: 좋아요 추가됨, false: 좋아요 취소됨
	 */
	@Transactional
	public boolean toggleLike(long userId, long targetId, TargetType targetType) {
		validateLikeTarget(targetId, targetType);

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
	 * 대상의 좋아요 수 조회
	 */
	@Transactional(readOnly = true)
	public Long getLikeCount(long targetId, TargetType targetType) {
		validateLikeTarget(targetId, targetType);
		return likeRepository.countByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 특정 대상에 대해 사용자가 좋아요를 눌렀는지 여부를 확인합니다.
	 *
	 * @return true - 좋아요를 눌렀음, false - 좋아요를 누르지 않음
	 */
	@Transactional(readOnly = true)
	public Boolean getLikeStatus(long userId, long targetId, TargetType targetType) {
		validateLikeTarget(targetId, targetType);
		return likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
	}

	/**
	 * 대상의 모든 좋아요 삭제 (대상이 삭제될 때 호출)
	 */
	@Transactional
	public void deleteAllLikes(long targetId, TargetType targetType) {
		validateLikeTarget(targetId, targetType);
		likeRepository.deleteByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 좋아요 대상 유효성 검증
	 */
	private void validateLikeTarget(long targetId, TargetType targetType) {
		if (targetId <= 0) {
			ErrorCode.INVALID_TARGET_ID.throwServiceException();
		}

	}

	/**
	 * 좋아요 삭제 및 대상 좋아요 카운트 감소
	 */
	private void deleteLike(Like like, long targetId, TargetType targetType) {
		likeTargetService.updateLikeCount(targetId, targetType, -1);
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

		likeTargetService.updateLikeCount(targetId, targetType, 1);
		likeRepository.save(like);
	}

}
