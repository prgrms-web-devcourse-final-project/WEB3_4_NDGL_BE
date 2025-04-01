package com.ndgl.spotfinder.domain.like.dto;

/**
 * 좋아요 상태 정보를 담는 클래스
 *
 * @param hasLiked  현재 사용자가 좋아요 했는지 여부
 * @param likeCount 총 좋아요 수
 */
public record LikeStatus(
	boolean hasLiked,
	long likeCount
) {
}
