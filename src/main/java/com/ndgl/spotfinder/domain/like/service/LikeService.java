package com.ndgl.spotfinder.domain.like.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
import com.ndgl.spotfinder.domain.like.entity.Likeable;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final UserService userService;
	private final PostRepository postRepository;
	private final PostCommentRepository postCommentRepository;

	/**
	 * 좋아요 추가 또는 삭제
	 *
	 * @return true: 좋아요 추가됨, false: 좋아요 취소됨
	 */
	@Transactional
	public boolean toggleLike(String email, Long targetId, TargetType targetType) {
		Long userId = Optional.ofNullable(email)
			.map(userService::findUserByEmail)
			.map(User::getId)
			.orElseThrow(ErrorCode.LIKE_INVALID_EMAIL::throwServiceException);

		return likeRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)
			.map(like -> {
				removeLike(like, targetId, targetType);
				return false;
			})
			.orElseGet(() -> {
				addLike(userId, targetId, targetType);
				return true;
			});
	}

	/**
	 * 대상의 좋아요 수 조회
	 */
	@Transactional(readOnly = true)
	public Long getLikeCount(Long targetId, TargetType targetType) {
		return likeRepository.countByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 특정 대상에 대해 사용자가 좋아요를 눌렀는지 여부를 확인합니다.
	 *
	 * @return true - 좋아요를 눌렀음, false - 좋아요를 누르지 않음
	 */
	@Transactional(readOnly = true)
	public Boolean getLikeStatus(Long userId, Long targetId, TargetType targetType) {
		return Optional.ofNullable(userId)
			.map(id -> likeRepository.existsByUserIdAndTargetIdAndTargetType(id, targetId, targetType))
			.orElse(false);
	}

	/**
	 * 대상의 모든 좋아요 삭제 (대상이 삭제될 때 호출)
	 */
	@Transactional
	public void deleteAllLikes(Long targetId, TargetType targetType) {
		likeRepository.deleteByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 좋아요 상태를 한번에 조회
	 */
	@Transactional(readOnly = true)
	public Map<Long, Boolean> getAllLikeStatus(long userId, List<Long> targetIds, Like.TargetType targetType) {
		if (targetIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<Long> likedTargetIds = likeRepository.findAllByUserIdAndTargetIdInAndTargetType(
				userId, targetIds, targetType)
			.stream()
			.map(Like::getTargetId)
			.toList();

		Map<Long, Boolean> result = new HashMap<>();
		for (Long targetId : targetIds) {
			result.put(targetId, likedTargetIds.contains(targetId));
		}
		return result;
	}

	private void addLike(Long userId, Long targetId, TargetType targetType) {
		Likeable target = getTarget(targetId, targetType);
		target.addLike();
		User user = userService.findUserById(userId);
		Like like = Like.builder()
			.user(user)
			.targetId(targetId)
			.targetType(targetType)
			.build();
		likeRepository.save(like);
	}

	private void removeLike(Like like, Long targetId, TargetType targetType) {
		Likeable target = getTarget(targetId, targetType);
		target.removeLike();
		likeRepository.delete(like);
	}

	private Likeable getTarget(Long targetId, TargetType targetType) {
		return switch (targetType) {
			case POST -> postRepository.findById(targetId)
				.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);
			case COMMENT -> postCommentRepository.findById(targetId)
				.orElseThrow(ErrorCode.COMMENT_NOT_FOUND::throwServiceException);
		};
	}
}
