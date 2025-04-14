package com.ndgl.spotfinder.domain.like.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

	@Mock
	private LikeRepository likeRepository;

	@Mock
	private PostRepository postRepository;

	@Mock
	private PostCommentRepository postCommentRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private LikeService likeService;

	private final long VALID_USER_ID = 1L;
	private final String VALID_USER_EMAIL = "test@example.com";
	private final long VALID_POST_ID = 10L;
	private final long VALID_COMMENT_ID = 20L;

	private User testUser;
	private Like postLike;
	private Like commentLike;
	private Post mockPostEntity;
	private PostComment mockCommentEntity;

	@BeforeEach
	public void setup() {
		// 테스트 객체 초기화
		testUser = User.builder()
			.id(VALID_USER_ID)
			.email(VALID_USER_EMAIL)
			.nickName("테스트유저")
			.blogName("테스트블로그")
			.build();

		postLike = Like.builder()
			.id(1L)
			.user(testUser)
			.targetId(VALID_POST_ID)
			.targetType(TargetType.POST)
			.build();

		commentLike = Like.builder()
			.id(2L)
			.user(testUser)
			.targetId(VALID_COMMENT_ID)
			.targetType(TargetType.COMMENT)
			.build();

		mockPostEntity = mock(Post.class);
		mockCommentEntity = mock(PostComment.class);
	}

	@Test
	@DisplayName("포스트 좋아요 추가 성공")
	public void toggleLike_addPostLike_success() {
		// given
		when(userService.findUserByEmail(VALID_USER_EMAIL)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST))
			.thenReturn(Optional.empty());
		when(likeRepository.save(any(Like.class))).thenReturn(postLike);
		when(postRepository.findById(VALID_POST_ID)).thenReturn(Optional.of(mockPostEntity));

		// when
		boolean result = likeService.toggleLike(VALID_USER_EMAIL, VALID_POST_ID, TargetType.POST);

		// then
		assertTrue(result);
		verify(userService).findUserByEmail(VALID_USER_EMAIL);
		verify(likeRepository).findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST);
		verify(likeRepository).save(any(Like.class));
		verify(postRepository).findById(VALID_POST_ID);
		verify(mockPostEntity).addLike();
	}

	@Test
	@DisplayName("포스트 좋아요 취소 성공")
	public void toggleLike_removePostLike_success() {
		// given
		when(userService.findUserByEmail(VALID_USER_EMAIL)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST))
			.thenReturn(Optional.of(postLike));
		when(postRepository.findById(VALID_POST_ID)).thenReturn(Optional.of(mockPostEntity));

		// when
		boolean result = likeService.toggleLike(VALID_USER_EMAIL, VALID_POST_ID, TargetType.POST);

		// then
		assertFalse(result);
		verify(userService).findUserByEmail(VALID_USER_EMAIL);
		verify(likeRepository).findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_POST_ID, TargetType.POST);
		verify(likeRepository).delete(postLike);
		verify(postRepository).findById(VALID_POST_ID);
		verify(mockPostEntity).removeLike();
	}

	@Test
	@DisplayName("댓글 좋아요 추가 성공")
	public void toggleLike_addCommentLike_success() {
		// given
		when(userService.findUserByEmail(VALID_USER_EMAIL)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT))
			.thenReturn(Optional.empty());
		when(likeRepository.save(any(Like.class))).thenReturn(commentLike);
		when(postCommentRepository.findById(VALID_COMMENT_ID)).thenReturn(Optional.of(mockCommentEntity));

		// when
		boolean result = likeService.toggleLike(VALID_USER_EMAIL, VALID_COMMENT_ID, TargetType.COMMENT);

		// then
		assertTrue(result);
		verify(userService).findUserByEmail(VALID_USER_EMAIL);
		verify(likeRepository).findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT);
		verify(likeRepository).save(any(Like.class));
		verify(postCommentRepository).findById(VALID_COMMENT_ID);
		verify(mockCommentEntity).addLike();
	}

	@Test
	@DisplayName("댓글 좋아요 취소 성공")
	public void toggleLike_removeCommentLike_success() {
		// given
		when(userService.findUserByEmail(VALID_USER_EMAIL)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT))
			.thenReturn(Optional.of(commentLike));
		when(postCommentRepository.findById(VALID_COMMENT_ID)).thenReturn(Optional.of(mockCommentEntity));

		// when
		boolean result = likeService.toggleLike(VALID_USER_EMAIL, VALID_COMMENT_ID, TargetType.COMMENT);

		// then
		assertFalse(result);
		verify(userService).findUserByEmail(VALID_USER_EMAIL);
		verify(likeRepository).findByUserIdAndTargetIdAndTargetType(VALID_USER_ID, VALID_COMMENT_ID,
			TargetType.COMMENT);
		verify(likeRepository).delete(commentLike);
		verify(postCommentRepository).findById(VALID_COMMENT_ID);
		verify(mockCommentEntity).removeLike();
	}

	@Test
	@DisplayName("이메일이 null일 때 좋아요 추가 실패")
	public void toggleLike_nullEmail_throwsException() {
		// when & then
		assertThrows(ServiceException.class, () -> {
			likeService.toggleLike(null, VALID_POST_ID, TargetType.POST);
		});
		verify(userService, never()).findUserByEmail(anyString());
		verify(likeRepository, never()).findByUserIdAndTargetIdAndTargetType(anyLong(), anyLong(), any());
	}

	@Test
	@DisplayName("여러 게시물의 좋아요 상태 한 번에 조회 성공")
	public void getAllLikeStatus_success() {
		// given
		List<Long> postIds = Arrays.asList(1L, 2L, 3L);
		List<Like> likes = Arrays.asList(
			Like.builder().user(testUser).targetId(1L).targetType(TargetType.POST).build(),
			Like.builder().user(testUser).targetId(3L).targetType(TargetType.POST).build()
		);

		when(likeRepository.findAllByUserIdAndTargetIdInAndTargetType(
			VALID_USER_ID, postIds, TargetType.POST)).thenReturn(likes);

		// when
		Map<Long, Boolean> result = likeService.getAllLikeStatus(VALID_USER_ID, postIds, TargetType.POST);

		// then
		assertEquals(3, result.size());
		assertTrue(result.get(1L));  // 좋아요 있음
		assertFalse(result.get(2L)); // 좋아요 없음
		assertTrue(result.get(3L));  // 좋아요 있음
		verify(likeRepository).findAllByUserIdAndTargetIdInAndTargetType(
			VALID_USER_ID, postIds, TargetType.POST);
	}

	@Test
	@DisplayName("빈 게시물 ID 리스트로 좋아요 상태 조회시 빈 맵 반환")
	public void getAllLikeStatus_emptyList_returnsEmptyMap() {
		// given
		List<Long> emptyPostIds = Collections.emptyList();

		// when
		Map<Long, Boolean> result = likeService.getAllLikeStatus(VALID_USER_ID, emptyPostIds, TargetType.POST);

		// then
		assertTrue(result.isEmpty());
		verify(likeRepository, never()).findAllByUserIdAndTargetIdInAndTargetType(
			anyLong(), anyList(), any(TargetType.class));
	}

	@Test
	@DisplayName("포스트의 모든 좋아요 삭제 성공")
	public void deleteAllLikes_forPost_success() {
		// when
		likeService.deleteAllLikes(VALID_POST_ID, TargetType.POST);

		// then
		verify(likeRepository).deleteByTargetIdAndTargetType(VALID_POST_ID, TargetType.POST);
	}

	@Test
	@DisplayName("댓글의 모든 좋아요 삭제 성공")
	public void deleteAllLikes_forComment_success() {
		// when
		likeService.deleteAllLikes(VALID_COMMENT_ID, TargetType.COMMENT);

		// then
		verify(likeRepository).deleteByTargetIdAndTargetType(VALID_COMMENT_ID, TargetType.COMMENT);
	}

	@Test
	@DisplayName("포스트 좋아요 수 조회 성공")
	public void getLikeCount_forPost_success() {
		// given
		long expectedCount = 5L;
		when(likeRepository.countByTargetIdAndTargetType(VALID_POST_ID, TargetType.POST))
			.thenReturn(expectedCount);

		// when
		Long result = likeService.getLikeCount(VALID_POST_ID, TargetType.POST);

		// then
		assertEquals(expectedCount, result);
		verify(likeRepository).countByTargetIdAndTargetType(VALID_POST_ID, TargetType.POST);
	}

	@Test
	@DisplayName("댓글 좋아요 수 조회 성공")
	public void getLikeCount_forComment_success() {
		// given
		long expectedCount = 3L;
		when(likeRepository.countByTargetIdAndTargetType(VALID_COMMENT_ID, TargetType.COMMENT))
			.thenReturn(expectedCount);

		// when
		Long result = likeService.getLikeCount(VALID_COMMENT_ID, TargetType.COMMENT);

		// then
		assertEquals(expectedCount, result);
		verify(likeRepository).countByTargetIdAndTargetType(VALID_COMMENT_ID, TargetType.COMMENT);
	}

	@Test
	@DisplayName("포스트 좋아요 상태 조회 - 좋아요가 존재함")
	public void getLikeStatus_forPost_exists() {
		// given
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(
			VALID_USER_ID, VALID_POST_ID, TargetType.POST)).thenReturn(true);

		// when
		Boolean result = likeService.getLikeStatus(VALID_USER_ID, VALID_POST_ID, TargetType.POST);

		// then
		assertTrue(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(
			VALID_USER_ID, VALID_POST_ID, TargetType.POST);
	}

	@Test
	@DisplayName("포스트 좋아요 상태 조회 - 좋아요 없음")
	public void getLikeStatus_forPost_notExists() {
		// given
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(
			VALID_USER_ID, VALID_POST_ID, TargetType.POST)).thenReturn(false);

		// when
		Boolean result = likeService.getLikeStatus(VALID_USER_ID, VALID_POST_ID, TargetType.POST);

		// then
		assertFalse(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(
			VALID_USER_ID, VALID_POST_ID, TargetType.POST);
	}

	@Test
	@DisplayName("댓글 좋아요 상태 조회 - 좋아요가 존재함")
	public void getLikeStatus_forComment_exists() {
		// given
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(
			VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT)).thenReturn(true);

		// when
		Boolean result = likeService.getLikeStatus(VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT);

		// then
		assertTrue(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(
			VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT);
	}

	@Test
	@DisplayName("댓글 좋아요 상태 조회 - 좋아요 없음")
	public void getLikeStatus_forComment_notExists() {
		// given
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(
			VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT)).thenReturn(false);

		// when
		Boolean result = likeService.getLikeStatus(VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT);

		// then
		assertFalse(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(
			VALID_USER_ID, VALID_COMMENT_ID, TargetType.COMMENT);
	}

	@Test
	@DisplayName("userId가 null인 경우 좋아요 상태 조회 - false 반환")
	public void getLikeStatus_userIdNull_returnsFalse() {
		// when
		Boolean result = likeService.getLikeStatus(null, VALID_POST_ID, TargetType.POST);

		// then
		assertFalse(result);
		verify(likeRepository, never()).existsByUserIdAndTargetIdAndTargetType(
			anyLong(), anyLong(), any(TargetType.class));
	}
}
