package com.ndgl.spotfinder.domain.like.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.service.PostCommentService;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ServiceException;

/**
 * @see LikeService
 */
@ActiveProfiles("test")
@SpringBootTest
public class LikeServiceTest {
	@InjectMocks
	private LikeService likeService;

	@Mock
	private LikeRepository likeRepository;

	@Mock
	private UserService userService;

	@Mock
	private PostService postService;

	@Mock
	private PostCommentService postCommentService;

	@Mock
	private UserRepository userRepository;

	private final User testUser = User.builder()
		.id(1L)
		.email("test@example.com")
		.nickName("테스트유저")
		.blogName("테스트블로그")
		.build();

	private final Like postLike = Like.builder()
		.id(1L)
		.user(testUser)
		.targetId(10L)
		.targetType(TargetType.POST)
		.build();

	private final Like commentLike = Like.builder()
		.id(2L)
		.user(testUser)
		.targetId(20L)
		.targetType(TargetType.COMMENT)
		.build();

	@Test
	@DisplayName("포스트 좋아요 추가 성공")
	public void toggleLike_addPostLike_success() {
		// given
		long userId = 1L;
		long postId = 10L;
		Post post = mock(Post.class);

		// when
		when(userService.findUserById(userId)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST))
			.thenReturn(Optional.empty());
		when(postService.findPostById(postId)).thenReturn(post);
		when(likeRepository.save(any(Like.class))).thenReturn(postLike);

		boolean result = likeService.toggleLike(userId, postId, TargetType.POST);

		// then
		assertTrue(result);
		verify(likeRepository).save(any(Like.class));
		verify(post).updateLikeCount(1);
	}

	@Test
	@DisplayName("포스트 좋아요 취소 성공")
	public void toggleLike_removePostLike_success() {
		// given
		long userId = 1L;
		long postId = 10L;
		Post post = mock(Post.class);

		// when
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST))
			.thenReturn(Optional.of(postLike));
		when(postService.findPostById(postId)).thenReturn(post);

		boolean result = likeService.toggleLike(userId, postId, TargetType.POST);

		// then
		assertFalse(result);
		verify(likeRepository).delete(postLike);
		verify(post).updateLikeCount(-1);
	}

	@Test
	@DisplayName("댓글 좋아요 추가 성공")
	public void toggleLike_addCommentLike_success() {
		// given
		long userId = 1L;
		long commentId = 20L;
		PostComment comment = mock(PostComment.class);

		// when
		when(userService.findUserById(userId)).thenReturn(testUser);
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT))
			.thenReturn(Optional.empty());
		when(postCommentService.findCommentById(commentId)).thenReturn(comment);
		when(likeRepository.save(any(Like.class))).thenReturn(commentLike);

		boolean result = likeService.toggleLike(userId, commentId, TargetType.COMMENT);

		// then
		assertTrue(result);
		verify(likeRepository).save(any(Like.class));
		verify(comment).updateLikeCount(1);
	}

	@Test
	@DisplayName("댓글 좋아요 취소 성공")
	public void toggleLike_removeCommentLike_success() {
		// given
		long userId = 1L;
		long commentId = 20L;
		PostComment comment = mock(PostComment.class);

		// when
		when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT))
			.thenReturn(Optional.of(commentLike));
		when(postCommentService.findCommentById(commentId)).thenReturn(comment);

		boolean result = likeService.toggleLike(userId, commentId, TargetType.COMMENT);

		// then
		assertFalse(result);
		verify(likeRepository).delete(commentLike);
		verify(comment).updateLikeCount(-1);
	}

	@Test
	@DisplayName("좋아요 추가 실패 - 잘못된 타겟 ID")
	public void toggleLike_invalidTargetId() {
		// given
		long userId = 1L;
		long invalidTargetId = -1L;

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> likeService.toggleLike(userId, invalidTargetId, TargetType.POST));
		assertNotNull(exception);
	}

	@Test
	@DisplayName("포스트의 모든 좋아요 삭제 성공")
	public void deleteAllLikes_forPost_success() {
		// given
		long postId = 10L;

		// when
		likeService.deleteAllLikes(postId, TargetType.POST);

		// then
		verify(likeRepository).deleteByTargetIdAndTargetType(postId, TargetType.POST);
	}

	@Test
	@DisplayName("댓글의 모든 좋아요 삭제 성공")
	public void deleteAllLikes_forComment_success() {
		// given
		long commentId = 20L;

		// when
		likeService.deleteAllLikes(commentId, TargetType.COMMENT);

		// then
		verify(likeRepository).deleteByTargetIdAndTargetType(commentId, TargetType.COMMENT);
	}

	@Test
	@DisplayName("포스트 좋아요 수 조회 성공")
	public void getLikeCount_forPost_success() {
		// given
		long postId = 10L;
		long expectedCount = 5L;

		// when
		when(likeRepository.countByTargetIdAndTargetType(postId, TargetType.POST))
			.thenReturn(expectedCount);

		Long result = likeService.getLikeCount(postId, TargetType.POST);

		// then
		assertEquals(expectedCount, result);
		verify(likeRepository).countByTargetIdAndTargetType(postId, TargetType.POST);
	}

	@Test
	@DisplayName("댓글 좋아요 수 조회 성공")
	public void getLikeCount_forComment_success() {
		// given
		long commentId = 20L;
		long expectedCount = 3L;

		// when
		when(likeRepository.countByTargetIdAndTargetType(commentId, TargetType.COMMENT))
			.thenReturn(expectedCount);

		Long result = likeService.getLikeCount(commentId, TargetType.COMMENT);

		// then
		assertEquals(expectedCount, result);
		verify(likeRepository).countByTargetIdAndTargetType(commentId, TargetType.COMMENT);
	}

	@Test
	@DisplayName("좋아요 수 조회 - 존재하지 않는 ID")
	public void getLikeCount_invalidTargetId() {
		// given
		long invalidTargetId = -1L;

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> likeService.getLikeCount(invalidTargetId, TargetType.POST));
		assertNotNull(exception);
	}

	@Test
	@DisplayName("포스트 좋아요 상태 조회 - 좋아요 있음")
	public void getLikeStatus_forPost_exists() {
		// given
		long userId = 1L;
		long postId = 10L;

		// when
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST))
			.thenReturn(true);

		Boolean result = likeService.getLikeStatus(userId, postId, TargetType.POST);

		// then
		assertTrue(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST);
	}

	@Test
	@DisplayName("포스트 좋아요 상태 조회 - 좋아요 없음")
	public void getLikeStatus_forPost_notExists() {
		// given
		long userId = 1L;
		long postId = 10L;

		// when
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST))
			.thenReturn(false);

		Boolean result = likeService.getLikeStatus(userId, postId, TargetType.POST);

		// then
		assertFalse(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST);
	}

	@Test
	@DisplayName("댓글 좋아요 상태 조회 - 좋아요 있음")
	public void getLikeStatus_forComment_exists() {
		// given
		long userId = 1L;
		long commentId = 20L;

		// when
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT))
			.thenReturn(true);

		Boolean result = likeService.getLikeStatus(userId, commentId, TargetType.COMMENT);

		// then
		assertTrue(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT);
	}

	@Test
	@DisplayName("댓글 좋아요 상태 조회 - 좋아요 없음")
	public void getLikeStatus_forComment_notExists() {
		// given
		long userId = 1L;
		long commentId = 20L;

		// when
		when(likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT))
			.thenReturn(false);

		Boolean result = likeService.getLikeStatus(userId, commentId, TargetType.COMMENT);

		// then
		assertFalse(result);
		verify(likeRepository).existsByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT);
	}
}
