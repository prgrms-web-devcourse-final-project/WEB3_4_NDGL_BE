package com.ndgl.spotfinder.domain.like.service;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeTargetService {
	private final PostRepository postRepository;
	private final PostCommentRepository postCommentRepository;

	/**
	 * 게시물 또는 댓글의 좋아요 수를 업데이트합니다.
	 *
	 * @param targetId   좋아요 대상 ID
	 * @param targetType 대상 타입 (POST 또는 COMMENT)
	 * @param delta      변화량 (+1: 좋아요 추가, -1: 좋아요 취소)
	 */
	public void updateLikeCount(long targetId, TargetType targetType, int delta) {
		switch (targetType) {
			case POST -> {
				Post post = postRepository.findById(targetId)
					.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);
				post.updateLikeCount(delta);
			}
			case COMMENT -> {
				PostComment comment = postCommentRepository.findById(targetId)
					.orElseThrow(ErrorCode.COMMENT_NOT_FOUND::throwServiceException);
				comment.updateLikeCount(delta);
			}
			default -> com.ndgl.spotfinder.global.exception.ErrorCode.UNSUPPORTED_TARGET_TYPE.throwServiceException();
		}
	}
} 