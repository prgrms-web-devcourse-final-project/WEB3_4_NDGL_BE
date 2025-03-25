package com.ndgl.spotfinder.domain.like.service;

import static com.ndgl.spotfinder.domain.like.entity.Likes.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.like.entity.Likes;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

	private final LikeRepository likeRepository;

	/**
	 * 사용자가 대상에 좋아요를 눌렀는지 확인
	 */
	public boolean hasUserLiked(long userId, long targetId, TargetType targetType) {
		return likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
	}

	/**
	 * 대상(포스트/댓글)의 좋아요 개수 조회
	 */
	public long getLikeCount(long targetId, TargetType targetType) {
		return likeRepository.countByTargetIdAndTargetType(targetId, targetType);
	}

	/**
	 * 사용자가 좋아요한 대상(포스트/댓글) ID 목록 조회
	 */
	public List<Long> getLikedTargetIds(long userId, TargetType targetType) {
		return likeRepository.findByUserIdAndTargetType(userId, targetType)
			.stream()
			.map(Likes::getTargetId)
			.toList();
	}

	/**
	 * 좋아요 추가 (어떤 타입이든 공통 처리)
	 */
	@Transactional
	public void addLike(long userId, long targetId, TargetType targetType) {
		// 이미 좋아요를 눌렀는지 확인
		if (hasUserLiked(userId, targetId, targetType)) {
			return; // 이미 좋아요가 있으므로 아무것도 하지 않음
		}

		// 타입에 따라 적절한 Like 객체 생성
		Likes likes;
		if (targetType == TargetType.POST) {
			likes = Likes.createPostLike(userId, targetId);
		} else if (targetType == TargetType.COMMENT) {
			likes = Likes.createCommentLike(userId, targetId);
		} else {
			throw new IllegalArgumentException("지원하지 않는 좋아요 타입입니다: " + targetType);
		}

		// 좋아요 저장
		likeRepository.save(likes);
	}

	/**
	 * 포스트에 좋아요 추가
	 */
	@Transactional
	public void addPostLike(long userId, long postId) {
		addLike(userId, postId, TargetType.POST);
	}

	/**
	 * 댓글에 좋아요 추가
	 */
	@Transactional
	public void addCommentLike(long userId, long commentId) {
		addLike(userId, commentId, TargetType.COMMENT);
	}

	/**
	 * 좋아요 삭제 (어떤 타입이든 공통 처리)
	 */
	@Transactional
	public void deleteLike(long userId, long targetId, TargetType targetType) {
		// 좋아요 조회
		Optional<Likes> likeOpt = likeRepository.findByUserIdAndTargetIdAndTargetType(
			userId, targetId, targetType
		);

		// 좋아요가 있으면 삭제
		likeOpt.ifPresent(likeRepository::delete);
	}

	/**
	 * 댓글 좋아요 삭제
	 */
	@Transactional
	public void deleteCommentLike(long userId, Long commentId) {
		deleteLike(userId, commentId, TargetType.COMMENT);
	}

	/**
	 * 포스트 좋아요 삭제
	 */
	@Transactional
	public void deletePostLike(long userId, Long postId) {
		deleteLike(userId, postId, TargetType.POST);
	}

	/**
	 * 특정 대상의 모든 좋아요 삭제 (예: 포스트나 댓글이 삭제될 때 호출)
	 */
	@Transactional
	public void deleteAllLikes(long targetId, TargetType targetType) {
		likeRepository.deleteByTargetIdAndTargetType(targetId, targetType);
	}
}
