package com.ndgl.spotfinder.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentRequestDto;
import com.ndgl.spotfinder.domain.comment.dto.PostCommentResponseDto;
import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ServiceException;

@SpringBootTest
@ActiveProfiles("test")
public class PostCommentServiceTest {
	@InjectMocks
	private PostCommentService postCommentService;

	@Mock
	private PostCommentRepository postCommentRepository;

	@Mock
	private PostService postService;

	@Mock
	private UserService userService;

	@Mock
	private LikeService likeService;

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
		PostCommentRequestDto reqBody = new PostCommentRequestDto(content, null);

		when(postService.findPostById(postId)).thenReturn(post);
		when(userService.findUserByEmail(user.getEmail())).thenReturn(user);

		ArgumentCaptor<PostComment> captor = ArgumentCaptor.forClass(PostComment.class);
		doAnswer(invocation -> invocation.getArgument(0))
			.when(postCommentRepository).save(any(PostComment.class));

		// When
		postCommentService.write(postId, reqBody, user.getEmail());

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
		when(userService.findUserByEmail(user.getEmail())).thenReturn(user);
		postCommentService.modify(postId, commentId, content, user.getEmail());

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
			() -> postCommentService.modify(postId, commentId, content, user.getEmail())
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
		when(userService.findUserByEmail(user.getEmail())).thenReturn(user);
		postCommentService.delete(postId, commentId, user.getEmail());

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
			() -> postCommentService.delete(postId, commentId, user.getEmail())
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
		PostCommentResponseDto result = postCommentService.getComment(null, postId, commentId);

		// Then
		assertNotNull(result);
		assertEquals(commentId, result.id());
		assertEquals(postId, result.postId());
		assertEquals(comment.getContent(), result.content());
	}

	@Test
	@DisplayName("존재하지 않는 댓글 조회")
	void getComment_notFound() {
		// Given
		Long postId = 1L;
		Long commentId = 10L;

		// When & Then
		when(postCommentRepository.findById(commentId)).thenReturn(Optional.empty());
		assertThrows(ServiceException.class, () -> postCommentService.getComment(null, postId, commentId),
			"댓글이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("포스트에 속하지 않는 댓글 조회")
	void getComment_notInPost() {
		// Given
		Long postId = 2L;
		Long commentId = 1L;

		// When & Then
		when(postCommentRepository.findById(commentId)).thenReturn(Optional.empty());
		assertThrows(ServiceException.class, () -> postCommentService.getComment(null, postId, commentId),
			"해당 포스트의 댓글이 아닙니다.");
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

		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<PostComment> commentSlice = new SliceImpl<>(comments.subList(0, size), pageRequest, true);

		// Repository Stub 설정
		when(postCommentRepository.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, lastId,
			pageRequest))
			.thenReturn(commentSlice);

		// When
		SliceResponse<PostCommentResponseDto> response = postCommentService.getComments(null, postId, lastId, size);

		// Then
		assertNotNull(response);
		assertEquals(size, response.contents().size());
		assertTrue(response.hasNext());
		verify(postCommentRepository, times(1))
			.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, lastId, pageRequest);
	}

	@Test
	@DisplayName("댓글 목록 조회 - 마지막 페이지")
	void getComments_LastPage() {
		// Given
		Long postId = 1L;
		long lastId = 1L;
		int size = 3;

		List<PostComment> comments = List.of(comment2);
		PageRequest pageRequest = PageRequest.of(0, size);

		int toIndex = comments.size();
		Slice<PostComment> commentSlice = new SliceImpl<>(comments.subList(0, toIndex), pageRequest, false);

		// Repository Stub 설정
		when(postCommentRepository.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, lastId,
			pageRequest))
			.thenReturn(commentSlice);

		// When
		SliceResponse<PostCommentResponseDto> response = postCommentService.getComments(null, postId, lastId, size);

		// Then
		assertNotNull(response);
		assertEquals(toIndex, response.contents().size());
		assertFalse(response.hasNext());
		verify(postCommentRepository, times(1))
			.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, lastId, pageRequest);
	}

	@Test
	@DisplayName("댓글 목록 조회 - 빈 리스트 반환")
	void getComments_Empty() {
		// Given
		Long postId = 1L;
		long lastId = 10L;
		int size = 3;

		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<PostComment> emptySlice = new SliceImpl<>(Collections.emptyList(), pageRequest, false);

		when(postCommentRepository.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, lastId,
			pageRequest))
			.thenReturn(emptySlice);

		// When
		SliceResponse<PostCommentResponseDto> response = postCommentService.getComments(null, postId, lastId, size);

		// Then
		assertNotNull(response);
		assertTrue(response.contents().isEmpty());
		assertFalse(response.hasNext());
		verify(postCommentRepository, times(1))
			.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, lastId, pageRequest);
	}

	@Test
	@DisplayName("댓글 조회 - 비로그인 상태")
	void getComment_WithoutLogin() {
		// Given
		Long postId = 1L;
		Long commentId = 1L;
		String email = null;

		// When
		when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		PostCommentResponseDto result = postCommentService.getComment(email, postId, commentId);

		// Then
		assertNotNull(result);
		assertEquals(commentId, result.id());
		assertEquals("댓글 1", result.content());
		assertFalse(result.likeStatus());
		verify(likeService, never()).getLikeStatus(anyLong(), anyLong(), any());
	}

	@Test
	@DisplayName("댓글 목록 조회 - 좋아요 상태 확인")
	void getComments_WithLikeStatus() {
		// Given
		Long postId = 1L;
		long lastId = 0L;
		int size = 2;
		Long userId = 1L;

		List<PostComment> comments = List.of(comment, comment2);
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<PostComment> commentSlice = new SliceImpl<>(comments, pageRequest, false);

		when(postCommentRepository.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, lastId,
			pageRequest))
			.thenReturn(commentSlice);
		when(userService.findUserByEmail(user.getEmail())).thenReturn(user);

		List<Long> commentIds = List.of(1L, 2L);
		Map<Long, Boolean> likeStatusMap = new HashMap<>();
		likeStatusMap.put(1L, true);
		likeStatusMap.put(2L, false);

		when(likeService.getAllLikeStatus(userId, commentIds, Like.TargetType.COMMENT))
			.thenReturn(likeStatusMap);

		// When
		SliceResponse<PostCommentResponseDto> response = postCommentService.getComments(user.getEmail(), postId, lastId,
			size);

		// Then
		assertNotNull(response);
		assertEquals(2, response.contents().size());
		assertTrue(response.contents().get(0).likeStatus());
		assertFalse(response.contents().get(1).likeStatus());

		verify(likeService, times(1)).getAllLikeStatus(anyLong(), anyList(), any());
	}

	@Test
	@DisplayName("댓글 고정")
	void pinComment_success() {
		// Given
		Long postId = 1L;
		Long commentId = 1L;

		when(postService.findPostById(postId)).thenReturn(post);
		when(userService.findUserByEmail(user.getEmail())).thenReturn(user);
		when(postCommentRepository.findPinnedCommentByPostId(postId)).thenReturn(Optional.empty());
		when(postCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		// When
		postCommentService.pinComment(postId, commentId, user.getEmail());

		// Then
		assertTrue(comment.isPinned());
	}

	@Test
	@DisplayName("포스트 작성자가 아닌 유저가 댓글 고정")
	void pinComment_unauthorized() {
		// Given
		User otherUser = User.builder().id(2L).email("other@test.com").build();

		when(postService.findPostById(post.getId())).thenReturn(post);
		when(userService.findUserByEmail(otherUser.getEmail())).thenReturn(otherUser);

		// When & Then
		assertThrows(ServiceException.class, () -> postCommentService.pinComment(
			1L, 1L, otherUser.getEmail()), "포스트 작성자만 댓글을 고정할 수 있습니다."
		);
	}

	@Test
	@DisplayName("존재하지 않는 댓글 고정")
	void pinComment_NotFound() {
		// Given
		Long invalidCommentId = 999L;

		when(postService.findPostById(post.getId())).thenReturn(post);
		when(userService.findUserByEmail(user.getEmail())).thenReturn(user);
		when(postCommentRepository.findPinnedCommentByPostId(post.getId())).thenReturn(Optional.empty());
		when(postCommentRepository.findById(invalidCommentId)).thenReturn(Optional.empty());

		// When & Then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> postCommentService.pinComment(post.getId(), invalidCommentId, user.getEmail())
		);
		assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
	}
}
