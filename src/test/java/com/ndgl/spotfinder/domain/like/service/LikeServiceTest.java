package com.ndgl.spotfinder.domain.like.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;

/**
 * TODO 연관관계 설정 시 userId 수정 필요
 *
 * @see LikeService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LikeServiceTest {

	@Autowired
	LikeService likeService;

	@Autowired
	LikeRepository likeRepository;

	private final Long userId = 1L;
	private final Long postId = 100L;

	@BeforeEach
	void setUp() {
		likeRepository.deleteAll();
	}

	@Test
	@DisplayName("포스트 좋아요 추가 테스트")
	void addPostLike() {
		boolean isAdded = likeService.togglePostLike(userId, postId);

		assertThat(isAdded).isTrue();

		// DB Check
		Optional<Like> savedLike = likeRepository.findByUserIdAndTargetIdAndTargetType(
			userId, postId, Like.TargetType.POST);

		assertThat(savedLike).isPresent();
		assertThat(savedLike.get().getUserId()).isEqualTo(userId);
		assertThat(savedLike.get().getTargetId()).isEqualTo(postId);
		assertThat(savedLike.get().getTargetType()).isEqualTo(Like.TargetType.POST);
	}

	@Test
	@DisplayName("포스트 좋아요 취소 테스트")
	void cancelPostLike() {
		//Before Cancel
		boolean isAdded = likeService.togglePostLike(userId, postId);

		assertThat(isAdded).isTrue();

		// DB Check
		Optional<Like> savedLike = likeRepository.findByUserIdAndTargetIdAndTargetType(
			userId, postId, Like.TargetType.POST);

		assertThat(savedLike).isPresent();
		assertThat(savedLike.get().getUserId()).isEqualTo(userId);
		assertThat(savedLike.get().getTargetId()).isEqualTo(postId);
		assertThat(savedLike.get().getTargetType()).isEqualTo(Like.TargetType.POST);

		// After Cancel
		isAdded = likeService.togglePostLike(userId, postId);

		assertThat(isAdded).isFalse();

		savedLike = likeRepository.findByUserIdAndTargetIdAndTargetType(
			userId, postId, Like.TargetType.POST
		);

		assertThat(savedLike).isEmpty();
	}

}