package com.ndgl.spotfinder.domain.like.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.like.dto.LikeStatus;
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
	private final Long commentId = 100L;

	@BeforeEach
	void setUp() {
		likeRepository.deleteAll();
	}

	@Nested
	@DisplayName("포스트 좋아요 관련 테스트")
	class PostLikeTest {

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

		@Test
		@DisplayName("포스트 중복 좋아요 에러 테스트")
		void duplicateLikeConstraintViolation() {
			Like firstLike = Like.createPostLike(userId, postId);
			likeRepository.save(firstLike);

			Like duplicateLike = Like.createPostLike(userId, postId);

			// 무결설 제약 위반
			assertThrows(DataIntegrityViolationException.class, () -> {
				likeRepository.save(duplicateLike);
				likeRepository.flush();
			});
		}

		@Test
		@DisplayName("포스트에 여러 사용자가 좋아요 추가 테스트")
		@Transactional
		void sameLikerForMultiplePosts() {
			long userId2 = userId + 1;
			long userId3 = userId + 2;

			likeService.togglePostLike(userId, postId);
			likeService.togglePostLike(userId2, postId);
			likeService.togglePostLike(userId3, postId);

			long count = likeRepository.countByTargetIdAndTargetType(postId, Like.TargetType.POST);

			assertEquals(3, count);
		}

		@Test
		@DisplayName("포스트 좋아요 상태 조회 테스트: 좋아요가 있을 경우")
		void getPostLikeStatus_WithLike() {
			Like like = Like.createPostLike(userId, postId);
			likeRepository.save(like);

			long userId2 = userId + 1;
			Like like2 = Like.createPostLike(userId2, postId);
			likeRepository.save(like2);

			LikeStatus status = likeService.getPostLikeStatus(userId, postId);

			assertThat(status.hasLiked()).isTrue();
			assertEquals(2, status.likeCount());
		}

		@Test
		@DisplayName("포스트 좋아요 상태 조회 테스트: 좋아요가 없을 경우")
		void getPostLikeStatus_NoLike() {
			LikeStatus status = likeService.getPostLikeStatus(userId, postId);

			assertThat(status.hasLiked()).isFalse();
			assertEquals(0, status.likeCount());
		}

		@Test
		@DisplayName("포스트 관련 좋아요 모두 삭제 테스트")
		void deleteAllLikesForPost_ShouldDeleteAll() {
			likeRepository.save(Like.createPostLike(userId, postId));
			likeRepository.save(Like.createPostLike(userId + 1, postId));
			likeRepository.save(Like.createPostLike(userId + 2, postId));

			long countBefore = likeRepository.countByTargetIdAndTargetType(postId, Like.TargetType.POST);
			assertEquals(3, countBefore);

			likeService.deleteAllLikesForPost(postId);

			long countAfter = likeRepository.countByTargetIdAndTargetType(postId, Like.TargetType.POST);
			assertEquals(0, countAfter);
		}
	}

	@Nested
	@DisplayName("댓글 좋아요 관련 테스트")
	class CommentLikeTest {

		@Test
		@DisplayName("댓글 좋아요 추가 테스트")
		void addCommentLike() {
			boolean isAdded = likeService.toggleCommentLike(userId, commentId);

			assertThat(isAdded).isTrue();

			// DB Check
			Optional<Like> savedLike = likeRepository.findByUserIdAndTargetIdAndTargetType(
				userId, commentId, Like.TargetType.COMMENT);

			assertThat(savedLike).isPresent();
			assertThat(savedLike.get().getUserId()).isEqualTo(userId);
			assertThat(savedLike.get().getTargetId()).isEqualTo(commentId);
			assertThat(savedLike.get().getTargetType()).isEqualTo(Like.TargetType.COMMENT);
		}

		@Test
		@DisplayName("댓글 좋아요 취소 테스트")
		void cancelCommentLike() {
			//Before Cancel
			boolean isAdded = likeService.toggleCommentLike(userId, commentId);

			assertThat(isAdded).isTrue();

			// DB Check
			Optional<Like> savedLike = likeRepository.findByUserIdAndTargetIdAndTargetType(
				userId, commentId, Like.TargetType.COMMENT);

			assertThat(savedLike).isPresent();
			assertThat(savedLike.get().getUserId()).isEqualTo(userId);
			assertThat(savedLike.get().getTargetId()).isEqualTo(commentId);
			assertThat(savedLike.get().getTargetType()).isEqualTo(Like.TargetType.COMMENT);

			// After Cancel
			isAdded = likeService.toggleCommentLike(userId, commentId);

			assertThat(isAdded).isFalse();

			savedLike = likeRepository.findByUserIdAndTargetIdAndTargetType(
				userId, commentId, Like.TargetType.COMMENT
			);

			assertThat(savedLike).isEmpty();
		}

		@Test
		@DisplayName("댓글 중복 좋아요 에러 테스트")
		void duplicateLikeConstraintViolation() {
			Like firstLike = Like.createCommentLike(userId, commentId);
			likeRepository.save(firstLike);

			Like duplicateLike = Like.createCommentLike(userId, commentId);

			// 무결설 제약 위반
			assertThrows(DataIntegrityViolationException.class, () -> {
				likeRepository.save(duplicateLike);
				likeRepository.flush();
			});
		}

		@Test
		@DisplayName("댓글에 여러 사용자가 좋아요 추가 테스트")
		@Transactional
		void sameLikerForMultipleComment() {
			long userId2 = userId + 1;
			long userId3 = userId + 2;

			likeService.toggleCommentLike(userId, commentId);
			likeService.toggleCommentLike(userId2, commentId);
			likeService.toggleCommentLike(userId3, commentId);

			long count = likeRepository.countByTargetIdAndTargetType(commentId, Like.TargetType.COMMENT);

			assertEquals(3, count);
		}

		@Test
		@DisplayName("댓글 좋아요 상태 조회 테스트: 좋아요가 있을 경우")
		void getCommentLikeStatus_WithLike() {
			Like like = Like.createCommentLike(userId, commentId);
			likeRepository.save(like);

			long userId2 = userId + 1;
			Like like2 = Like.createCommentLike(userId2, commentId);
			likeRepository.save(like2);

			LikeStatus status = likeService.getCommentLikeStatus(userId, commentId);

			assertThat(status.hasLiked()).isTrue();
			assertEquals(2, status.likeCount());
		}

		@Test
		@DisplayName("댓글 좋아요 상태 조회 테스트: 좋아요가 없을 경우")
		void getCommentLikeStatus_NoLike() {
			LikeStatus status = likeService.getCommentLikeStatus(userId, commentId);

			assertThat(status.hasLiked()).isFalse();
			assertEquals(0, status.likeCount());
		}


		@Test
		@DisplayName("댓글 관련 좋아요 모두 삭제 테스트")
		void deleteAllLikesForComment_ShouldDeleteAll() {
			likeRepository.save(Like.createCommentLike(userId, commentId));
			likeRepository.save(Like.createCommentLike(userId + 1, commentId));
			likeRepository.save(Like.createCommentLike(userId + 2, commentId));

			long countBefore = likeRepository.countByTargetIdAndTargetType(commentId, Like.TargetType.COMMENT);
			assertEquals(3, countBefore);

			likeService.deleteAllLikesForComment(commentId);

			long countAfter = likeRepository.countByTargetIdAndTargetType(commentId, Like.TargetType.COMMENT);
			assertEquals(0, countAfter);
		}

	}

}