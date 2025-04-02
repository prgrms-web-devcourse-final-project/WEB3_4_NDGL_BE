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

import com.ndgl.spotfinder.domain.like.dto.LikeStatus;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
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
    @DisplayName("포스트 종아요 추가 성공")
    public void togglePostLike_add_success() {
        // given
        long userId = 1L;
        long postId = 10L;

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST))
                .thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(postLike);

        boolean result = likeService.togglePostLike(userId, postId);

        // then
        assertTrue(result);
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("포스트 종아요 취소 성공")
    public void togglePostLike_remove_success() {
        // given
        long userId = 1L;
        long postId = 10L;

        // when
        when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST))
                .thenReturn(Optional.of(postLike));

        boolean result = likeService.togglePostLike(userId, postId);

        // then
        assertFalse(result);
        verify(likeRepository).delete(postLike);
    }

    @Test
    @DisplayName("댓글 종아요 추가 성공")
    public void toggleCommentLike_add_success() {
        // given
        long userId = 1L;
        long commentId = 20L;

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT))
                .thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(commentLike);

        boolean result = likeService.toggleCommentLike(userId, commentId);

        // then
        assertTrue(result);
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("댓글 종아요 취소 성공")
    public void toggleCommentLike_remove_success() {
        // given
        long userId = 1L;
        long commentId = 20L;

        // when
        when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT))
                .thenReturn(Optional.of(commentLike));

        boolean result = likeService.toggleCommentLike(userId, commentId);

        // then
        assertFalse(result);
        verify(likeRepository).delete(commentLike);
    }

    @Test
    @DisplayName("존재하지 않는 유저가 좋아요 사용시")
    public void toggleLike_userNotFound() {
        // given
        long userId = 999L;
        long targetId = 10L;

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(likeRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, TargetType.POST))
                .thenReturn(Optional.empty());

        // then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> likeService.toggleLike(userId, targetId, TargetType.POST));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("존재하지 않는 객체를 좋아요 할 시")
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
    @DisplayName("포스트 좋아요 상태 조회")
    public void getPostLikeStatus_success() {
        // given
        long userId = 1L;
        long postId = 10L;

        // when
        when(likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, postId, TargetType.POST))
                .thenReturn(true);
        when(likeRepository.countByTargetIdAndTargetType(postId, TargetType.POST))
                .thenReturn(5L);

        LikeStatus status = likeService.getPostLikeStatus(userId, postId);

        // then
        assertTrue(status.hasLiked());
        assertEquals(5L, status.likeCount());
    }

    @Test
    @DisplayName("댓글 좋아요 상태 조회")
    public void getCommentLikeStatus_success() {
        // given
        long userId = 1L;
        long commentId = 20L;

        // when
        when(likeRepository.existsByUserIdAndTargetIdAndTargetType(userId, commentId, TargetType.COMMENT))
                .thenReturn(false);
        when(likeRepository.countByTargetIdAndTargetType(commentId, TargetType.COMMENT))
                .thenReturn(3L);

        LikeStatus status = likeService.getCommentLikeStatus(userId, commentId);

        // then
        assertFalse(status.hasLiked());
        assertEquals(3L, status.likeCount());
    }

    @Test
    @DisplayName("좋아요 상태 조회 - 존재하지 않는 ID")
    public void getLikeStatus_invalidTargetId() {
        // given
        long userId = 1L;
        long invalidTargetId = -1L;

        // then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> likeService.getLikeStatus(userId, invalidTargetId, TargetType.POST));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("포스트의 모든 좋아요 삭제 성공")
    public void deleteAllLikesForPost_success() {
        // given
        long postId = 10L;

        // when
        likeService.deleteAllLikesForPost(postId);

        // then
        verify(likeRepository).deleteByTargetIdAndTargetType(postId, TargetType.POST);
    }

    @Test
    @DisplayName("댓글의 모든 좋아요 삭제 성공")
    public void deleteAllLikesForComment_success() {
        // given
        long commentId = 20L;

        // when
        likeService.deleteAllLikesForComment(commentId);

        // then
        verify(likeRepository).deleteByTargetIdAndTargetType(commentId, TargetType.COMMENT);
    }

    @Test
    @DisplayName("대상의 좋아요 삭제 성공")
    public void deleteAllLikes_success() {
        // given
        long targetId = 10L;
        TargetType targetType = TargetType.POST;

        // when
        likeService.deleteAllLikes(targetId, targetType);

        // then
        verify(likeRepository).deleteByTargetIdAndTargetType(targetId, targetType);
    }
} 