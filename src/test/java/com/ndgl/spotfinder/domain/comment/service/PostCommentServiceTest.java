package com.ndgl.spotfinder.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;

@SpringBootTest
@ActiveProfiles("test")
public class PostCommentServiceTest {
	@InjectMocks
	private PostCommentService postCommentService;

	@Mock
	private PostCommentRepository postCommentRepository;

	private final PostComment comment = PostComment.builder()
		.id(1L)
		.content("댓글 1")
		.postId(1L)
		.likeCount(0L)
		.build();

	@BeforeEach
	void setUp() {

	}

	@Test
	@DisplayName("댓글 작성")
	void createComment() {
		// Given
		Long postId = 1L;
		String content = "댓글 1";
		Long parentId = null;

		// When
		when(postCommentRepository.save(any(PostComment.class))).thenReturn(comment);
		postCommentService.write(postId, content, parentId);

		// Then
		verify(postCommentRepository, times(1)).save(any(PostComment.class));
		assertEquals("댓글 1", comment.getContent());
		assertEquals(postId, comment.getPostId());
		assertEquals(0L, comment.getLikeCount());
	}

	@Test
	@DisplayName("댓글 수정")
	void updateComment() {
		// Given
		Long postId = 1L;
		Long commentId = 1L;
		String content = "수정된 댓글 1";

		// When
		when(postCommentRepository.findById(commentId)).thenReturn(java.util.Optional.ofNullable(comment));
		postCommentService.modify(postId, commentId, content);

		// Then
		verify(postCommentRepository, times(1)).findById(commentId);
		assert comment != null;
		assertEquals(content, comment.getContent());
		assertEquals(postId, comment.getPostId());
		assertEquals(commentId, comment.getId());
		assertEquals(0L, comment.getLikeCount());
	}

	@Test
	@DisplayName("댓글 삭제")
	void deleteComment() {
		// Given
		Long postId = 1L;
		Long commentId = 1L;

		// When
		when(postCommentRepository.findById(commentId)).thenReturn(java.util.Optional.ofNullable(comment));
		postCommentService.delete(postId, commentId);

		// Then
		verify(postCommentRepository, times(1)).findById(commentId);
		assert comment != null;
		verify(postCommentRepository, times(1)).delete(comment);
	}
}
