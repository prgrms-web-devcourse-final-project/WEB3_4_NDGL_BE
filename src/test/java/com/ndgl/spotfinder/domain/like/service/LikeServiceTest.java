package com.ndgl.spotfinder.domain.like.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;

@ActiveProfiles("test")
@SpringBootTest
public class LikeServiceTest {

	@InjectMocks
	private LikeService likeService;

	@Mock
	private LikeRepository likeRepository;

	@Mock
	private LikeTargetService likeTargetService;

	@Mock
	private UserService userService;

	// 공통 테스트 상수 및 객체
	private final long VALID_USER_ID = 1L;
	private final String VALID_USER_EMAIL = "test@example.com";
	private final long VALID_POST_ID = 10L;
	private final long VALID_COMMENT_ID = 20L;
	private final long INVALID_TARGET_ID = -1L;

	private final User testUser = User.builder()
		.id(VALID_USER_ID)
		.email("test@example.com")
		.nickName("테스트유저")
		.blogName("테스트블로그")
		.build();

	private final Like postLike = Like.builder()
		.id(1L)
		.user(testUser)
		.targetId(VALID_POST_ID)
		.targetType(TargetType.POST)
		.build();

	private final Like commentLike = Like.builder()
		.id(2L)
		.user(testUser)
		.targetId(VALID_COMMENT_ID)
		.targetType(TargetType.COMMENT)
		.build();

	@Test
	@DisplayName("포스트 좋아요 추가 성공")
	public void toggleLike_addPostLike_success() {
		// given: 좋아요가 아직 존재하지 않음
		when(userService.findUserByEmail(VALID_USER_EMAIL)).thenReturn(testUser);
		when(userService.findUserById(VALID_USER_ID)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST))
			.thenReturn(Optional.empty());
		when(likeRepository.save(any(Like.class))).thenReturn(postLike);

		// when
		boolean result = likeService.toggleLike(VALID_USER_EMAIL, VALID_POST_ID, TargetType.POST);

		// then
		assertTrue(result);
		verify(userService).findUserByEmail(VALID_USER_EMAIL);
		verify(likeRepository).findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST);
		verify(likeRepository).save(any(Like.class));
		verify(likeTargetService).updateLikeCount(VALID_POST_ID, TargetType.POST, 1);
	}

	@Test
	@DisplayName("포스트 좋아요 취소 성공")
	public void toggleLike_removePostLike_success() {
		// given: 좋아요가 이미 존재함
		when(userService.findUserByEmail(VALID_USER_EMAIL)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST))
			.thenReturn(Optional.of(postLike));

		// when
		boolean result = likeService.toggleLike(VALID_USER_EMAIL, VALID_POST_ID, TargetType.POST);

		// then
		assertFalse(result);
		verify(userService).findUserByEmail(VALID_USER_EMAIL);
		verify(likeRepository).findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST);
		verify(likeRepository).delete(postLike);
		verify(likeTargetService).updateLikeCount(VALID_POST_ID, TargetType.POST, -1);
	}

	@Test
	@DisplayName("댓글 좋아요 추가 성공")
	public void toggleLike_addCommentLike_success() {
		// given: 좋아요가 아직 존재하지 않음 (댓글)
		when(userService.findUserByEmail(VALID_USER_EMAIL)).thenReturn(testUser);
		when(userService.findUserById(VALID_USER_ID)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT))
			.thenReturn(Optional.empty());
		when(likeRepository.save(any(Like.class))).thenReturn(commentLike);

		// when
		boolean result = likeService.toggleLike(VALID_USER_EMAIL, VALID_COMMENT_ID, TargetType.COMMENT);

		// then
		assertTrue(result);
		verify(userService).findUserByEmail(VALID_USER_EMAIL);
		verify(likeRepository).findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT);
		verify(likeRepository).save(any(Like.class));
		verify(likeTargetService).updateLikeCount(VALID_COMMENT_ID, TargetType.COMMENT, 1);
	}

	@Test
	@DisplayName("댓글 좋아요 취소 성공")
	public void toggleLike_removeCommentLike_success() {
		// given: 좋아요가 이미 존재함 (댓글)
		when(userService.findUserByEmail(VALID_USER_EMAIL)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT))
			.thenReturn(Optional.of(commentLike));

		// when
		boolean result = likeService.toggleLike(VALID_USER_EMAIL, VALID_COMMENT_ID, TargetType.COMMENT);

		// then
		assertFalse(result);
		verify(userService).findUserByEmail(VALID_USER_EMAIL);
		verify(likeRepository).findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT);
		verify(likeRepository).delete(commentLike);
		verify(likeTargetService).updateLikeCount(VALID_COMMENT_ID, TargetType.COMMENT, -1);
	}

	@Test
	@DisplayName("이메일 null 처리 실패")
	public void toggleLike_emailNull() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> likeService.toggleLike(null, VALID_POST_ID, TargetType.POST));
		assertNotNull(exception);
		assertEquals("Email은 null일 수 없습니다.", exception.getMessage());
	}

	@Test
	@DisplayName("포스트의 모든 좋아요 삭제 성공")
	public void deleteAllLikes_forPost_success() {
		likeService.deleteAllLikes(VALID_POST_ID, TargetType.POST);
		verify(likeRepository).deleteByTargetIdAndTargetType(VALID_POST_ID, TargetType.POST);
	}

	@Test
	@DisplayName("댓글의 모든 좋아요 삭제 성공")
	public void deleteAllLikes_forComment_success() {
		likeService.deleteAllLikes(VALID_COMMENT_ID, TargetType.COMMENT);
		verify(likeRepository).deleteByTargetIdAndTargetType(VALID_COMMENT_ID, TargetType.COMMENT);
	}

	@Test
	@DisplayName("포스트 좋아요 수 조회 성공")
	public void getLikeCount_forPost_success() {
		long expectedCount = 5L;
		when(likeRepository.countByTargetIdAndTargetType(VALID_POST_ID, TargetType.POST))
			.thenReturn(expectedCount);

		Long result = likeService.getLikeCount(VALID_POST_ID, TargetType.POST);
		assertEquals(expectedCount, result);
		verify(likeRepository).countByTargetIdAndTargetType(VALID_POST_ID, TargetType.POST);
	}

	@Test
	@DisplayName("댓글 좋아요 수 조회 성공")
	public void getLikeCount_forComment_success() {
		long expectedCount = 3L;
		when(likeRepository.countByTargetIdAndTargetType(VALID_COMMENT_ID, TargetType.COMMENT))
			.thenReturn(expectedCount);

		Long result = likeService.getLikeCount(VALID_COMMENT_ID, TargetType.COMMENT);
		assertEquals(expectedCount, result);
		verify(likeRepository).countByTargetIdAndTargetType(VALID_COMMENT_ID, TargetType.COMMENT);
	}

	@Test
	@DisplayName("포스트 좋아요 상태 조회 - 좋아요가 존재함")
	public void getLikeStatus_forPost_exists() {
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST))
			.thenReturn(true);

		Boolean result = likeService.getLikeStatus(VALID_USER_ID, VALID_POST_ID, TargetType.POST);
		assertTrue(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID,
			TargetType.POST);
	}

	@Test
	@DisplayName("포스트 좋아요 상태 조회 - 좋아요 없음")
	public void getLikeStatus_forPost_notExists() {
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST))
			.thenReturn(false);

		Boolean result = likeService.getLikeStatus(VALID_USER_ID, VALID_POST_ID, TargetType.POST);
		assertFalse(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID,
			TargetType.POST);
	}

	@Test
	@DisplayName("댓글 좋아요 상태 조회 - 좋아요가 존재함")
	public void getLikeStatus_forComment_exists() {
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT))
			.thenReturn(true);

		Boolean result = likeService.getLikeStatus(VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT);
		assertTrue(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT);
	}

	@Test
	@DisplayName("댓글 좋아요 상태 조회 - 좋아요 없음")
	public void getLikeStatus_forComment_notExists() {
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT))
			.thenReturn(false);

		Boolean result = likeService.getLikeStatus(VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT);
		assertFalse(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT);
	}

	@Test
	@DisplayName("userId가 null인 경우 좋아요 상태 조회 - false 반환")
	public void getLikeStatus_userIdNull_returnsFalse() {
		Boolean result = likeService.getLikeStatus(null, VALID_POST_ID, TargetType.POST);
		assertFalse(result);
		verify(likeRepository, never()).existsByUserIdAndTargetIdAndTargetType(anyLong(), anyLong(), any());
	}

}
