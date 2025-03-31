package com.ndgl.spotfinder.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentDto;
import com.ndgl.spotfinder.domain.comment.dto.PostCommentReqDto;
import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ServiceException;

@SpringBootTest
@ActiveProfiles("test")
public class PostCommentServiceTest {
	@InjectMocks
	private PostCommentService postCommentService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private PostCommentRepository postCommentRepository;

	@Mock
	private UserRepository userRepository;

	private final User user = User.builder()
		.id(1L)
		.email("test1@test.com")
		.nickName("testUser1")
		.blogName("blog1")
		.build();

	private final Post post = Post.builder()
		.id(1L)
		.title("제목1")
		.content("내용1")
		.user(user)
		.build();

	private final PostComment comment = PostComment.builder()
		.id(1L)
		.content("댓글 1")
		.user(user)
		.post(post)
		.likeCount(0L)
		.build();

	private final PostComment comment2 = PostComment.builder()
		.id(2L)
		.content("댓글 2")
		.user(user)
		.post(post)
		.likeCount(0L)
		.build();

	@Test
	@DisplayName("댓글 작성")
	void createComment() {
		// Given
		Long postId = 1L;
		String content = "댓글 3";
		PostCommentReqDto reqBody = new PostCommentReqDto(content, null);

		when(postRepository.findById(postId)).thenReturn(Optional.of(post));
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		ArgumentCaptor<PostComment> captor = ArgumentCaptor.forClass(PostComment.class);
		doAnswer(invocation -> invocation.getArgument(0))
			.when(postCommentRepository).save(any(PostComment.class));

		// When
		postCommentService.write(postId, reqBody, "test1@test.com");

		// Then
		verify(postCommentRepository, times(1)).save(captor.capture());
		PostComment savedComment = captor.getValue();

		assertEquals("댓글 3", savedComment.getContent());
		assertEquals(postId, savedComment.getPost().getId());
		assertEquals(0L, savedComment.getLikeCount());
		assertNull(savedComment.getParentComment()); // 대댓글이 아닌 경우
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
		assertEquals(postId, comment.getPost().getId());
		assertEquals(commentId, comment.getId());
		assertEquals(0L, comment.getLikeCount());
	}

	@Test
	@DisplayName("존재하지 않는 댓글 수정")
	void updateComment_notFound() {
		Long postId = 1L;
		Long commentId = 10L;
		String content = "수정된 댓글 10";

		when(postCommentRepository.findById(commentId)).thenReturn(Optional.empty());

		ServiceException exception = assertThrows(ServiceException.class,
			() -> postCommentService.modify(postId, commentId, content)
		);
		assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
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

	@Test
	@DisplayName("존재하지 않는 댓글 삭제")
	void deleteComment_notFound() {
		Long postId = 1L;
		Long commentId = 10L;

		when(postCommentRepository.findById(commentId)).thenReturn(Optional.empty());

		ServiceException exception = assertThrows(ServiceException.class,
			() -> postCommentService.delete(postId, commentId)
		);
		assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
	}

	@Test
	@DisplayName("댓글 조회")
	void getComment() {
		// Given
		Long postId = 1L;
		Long commentId = 1L;

		// When
		when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));
		PostCommentDto result = postCommentService.getComment(postId, commentId);

		// Then
		assertNotNull(result);
		assertEquals(commentId, result.getId());
		assertEquals(postId, result.getPostId());
		assertEquals(comment.getContent(), result.getContent());
	}

	@Test
	@DisplayName("존재하지 않는 댓글 조회")
	void getComment_notFound() {
		// Given
		Long postId = 1L;
		Long commentId = 10L;

		// When & Then
		when(postCommentRepository.findById(commentId)).thenReturn(Optional.empty());
		assertThrows(ServiceException.class, () -> postCommentService.getComment(postId, commentId), "댓글이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("포스트에 속하지 않는 댓글 조회")
	void getComment_notInPost() {
		// Given
		Long postId = 2L;
		Long commentId = 1L;

		// When & Then
		when(postCommentRepository.findById(commentId)).thenReturn(Optional.empty());
		assertThrows(ServiceException.class, () -> postCommentService.getComment(postId, commentId), "해당 포스트의 댓글이 아닙니다.");
	}

	@Test
	@DisplayName("댓글 목록 조회 - 정상 조회")
	void getComments_Success() {
		// Given
		Long postId = 1L;
		long lastId = 0L;
		int size = 2;

		PostComment comment3 = PostComment.builder().id(3L).content("댓글 3").post(post).likeCount(0L).build();
		PostComment comment4 = PostComment.builder().id(4L).content("댓글 4").post(post).likeCount(0L).build();
		PostComment comment5 = PostComment.builder().id(5L).content("댓글 5").post(post).likeCount(0L).build();

		List<PostComment> comments = List.of(comment, comment2, comment3, comment4, comment5);

		// Repository Stub 설정
		when(postCommentRepository.findByPostIdAndParentCommentIsNullAndIdGreaterThanOrderByIdAsc(postId, lastId))
			.thenReturn(comments);

		// When
		SliceResponse<PostCommentDto> response = postCommentService.getComments(postId, lastId, size);

		// Then
		assertNotNull(response);
		assertEquals(size, response.contents().size());
		assertTrue(response.hasNext());
		verify(postCommentRepository, times(1))
			.findByPostIdAndParentCommentIsNullAndIdGreaterThanOrderByIdAsc(postId, lastId);
	}

	@Test
	@DisplayName("댓글 목록 조회 - 마지막 페이지")
	void getComments_LastPage() {
		// Given
		Long postId = 1L;
		long lastId = 1L;
		int size = 3;

		List<PostComment> comments = List.of(comment2);

		// Repository Stub 설정
		when(postCommentRepository.findByPostIdAndParentCommentIsNullAndIdGreaterThanOrderByIdAsc(postId, lastId))
			.thenReturn(comments);

		// When
		SliceResponse<PostCommentDto> response = postCommentService.getComments(postId, lastId, size);

		// Then
		assertNotNull(response);
		assertEquals(comments.size(), response.contents().size());
		assertFalse(response.hasNext());
		verify(postCommentRepository, times(1))
			.findByPostIdAndParentCommentIsNullAndIdGreaterThanOrderByIdAsc(postId, lastId);
	}

	@Test
	@DisplayName("댓글 목록 조회 - 빈 리스트 반환")
	void getComments_Empty() {
		// Given
		Long postId = 1L;
		long lastId = 10L;
		int size = 3;

		when(postCommentRepository.findByPostIdAndParentCommentIsNullAndIdGreaterThanOrderByIdAsc(postId, lastId))
			.thenReturn(Collections.emptyList());

		// When
		SliceResponse<PostCommentDto> response = postCommentService.getComments(postId, lastId, size);

		// Then
		assertNotNull(response);
		assertTrue(response.contents().isEmpty());
		assertFalse(response.hasNext());
		verify(postCommentRepository, times(1))
			.findByPostIdAndParentCommentIsNullAndIdGreaterThanOrderByIdAsc(postId, lastId);
	}
}
