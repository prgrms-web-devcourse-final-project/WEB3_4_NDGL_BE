package com.ndgl.spotfinder.domain.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.ndgl.spotfinder.domain.image.service.ImageCleanupService;
import com.ndgl.spotfinder.domain.image.service.ImageService;
import com.ndgl.spotfinder.domain.like.service.LikeService;
import com.ndgl.spotfinder.domain.like.type.TargetType;
import com.ndgl.spotfinder.domain.post.dto.HashtagDto;
import com.ndgl.spotfinder.domain.post.dto.LocationDto;
import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostDetailResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostTempResponseDto;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Location;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.post.type.PostStatus;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ActiveProfiles("test")
@SpringBootTest
class PostServiceTest {
	@InjectMocks
	private PostService postService;

	@Mock
	private ImageCleanupService imageCleanupService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private UserService userService;

	@Mock
	private ImageService imageService;

	@Mock
	private LikeService likeService;

	private final User user1 = User.builder()
		.id(1L)
		.email("이메일1")
		.nickName("별명1")
		.blogName("블로그1")
		.build();

	private final User user2 = User.builder()
		.id(2L)
		.email("이메일2")
		.nickName("별명2")
		.blogName("블로그2")
		.build();

	private final Hashtag samplehashtag = Hashtag.builder()
		.id(1L)
		.name("태그1")
		.build();

	private final Location samplelocation = Location.builder()
		.id(1L)
		.name("장소1")
		.address("주소1")
		.latitude(37.0)
		.longitude(126.0)
		.build();

	private final Post samplePost = Post.builder()
		.id(1L)
		.title("제목1")
		.content("내용1")
		.user(user1)
		.build();

	@BeforeEach
	void setUp() {
		samplePost.addHashtag(samplehashtag);
		samplePost.addLocation(samplelocation);
	}

	@Test
	@DisplayName("포스트 생성 - 성공")
	void createPost_success() {
		// given
		HashtagDto hashtagDto = new HashtagDto("태그1");
		LocationDto locationDto = new LocationDto("장소1", "주소1", 37.0, 126.0, 1);
		PostCreateRequestDto requestDto = new PostCreateRequestDto(
			"제목1",
			"내용1",
			List.of(hashtagDto),
			List.of(locationDto),
			""
		);

		when(userService.findUserByEmail("이메일1")).thenReturn(user1);
		when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
			Post post = invocation.getArgument(0);
			ReflectionTestUtils.setField(post, "id", 1L);
			return post;
		});

		// when
		postService.createPost(requestDto, "이메일1");

		// then
		verify(postRepository, times(1)).save(any());
		ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
		verify(postRepository).save(postCaptor.capture());
		Post savedPost = postCaptor.getValue();
		assertEquals("제목1", savedPost.getTitle());
		assertEquals("내용1", savedPost.getContent());
		assertEquals("별명1", savedPost.getUser().getNickName());
		assertEquals(0, savedPost.getViewCount());
		assertEquals(0, savedPost.getLikeCount());
		assertEquals(1, savedPost.getHashtags().size());
		assertEquals(1, savedPost.getLocations().size());
	}

	@Test
	@DisplayName("포스트 생성 - 유저를 찾을 수 없는 경우")
	void createPost_userNotFound_fail() {
		// when
		ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
		when(userService.findUserByEmail("이메일1"))
			.thenThrow(new ServiceException(errorCode.getHttpStatus(), errorCode.getMessage()));

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> postService.createPost(any(PostCreateRequestDto.class), "이메일1"));
		assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
	}

	@Test
	@DisplayName("포스트 수정 - 요청한 사용자를 찾을 수 없는 경우")
	void updatePost_notFound_fail() {
		// given
		when(userService.findUserByEmail("이메일1")).thenReturn(user1);
		when(postRepository.findById(1L)).thenReturn(Optional.empty());

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> postService.updatePost(1L, any(PostUpdateRequestDto.class), "이메일1", PostStatus.PUBLIC));
		assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
	}

	@Test
	@DisplayName("포스트 수정 - 요청한 사용자가 작성자가 아닌 경우")
	void updatePost_AuthorMissMatch_fail() {
		// given
		when(userService.findUserByEmail("이메일2")).thenReturn(user2);
		when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> postService.updatePost(1L, any(PostUpdateRequestDto.class), "이메일2", PostStatus.PUBLIC));
		assertEquals(HttpStatus.FORBIDDEN, exception.getCode());
	}

	@Test
	@DisplayName("포스트 삭제 - 성공")
	void deletePost_success() {
		// given
		when(userService.findUserByEmail("이메일1")).thenReturn(user1);
		when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));

		// when
		postService.softDeletePost(1L, "이메일1");

		// then
		assertEquals(PostStatus.DELETED, samplePost.getStatus());
		assertEquals(LocalDate.now().plusWeeks(1), samplePost.getDeleteScheduledAt());
	}

	@Test
	@DisplayName("포스트 삭제 - 요청한 사용자를 찾을 수 없는 경우")
	void deletePost_notFound_fail() {
		// given
		when(userService.findUserByEmail("이메일1")).thenReturn(user1);
		when(postRepository.findById(1L)).thenReturn(Optional.empty());

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> postService.softDeletePost(1L, "이메일1"));
		assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
	}

	@Test
	@DisplayName("포스트 삭제 - 요청한 사용자가 작성자가 아닌 경우")
	void deletePost_AuthorMissMatch_fail() {
		// given
		when(userService.findUserByEmail("이메일2")).thenReturn(user2);
		when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> postService.softDeletePost(1L, "이메일2"));
		assertEquals(HttpStatus.FORBIDDEN, exception.getCode());
	}

	@Test
	@DisplayName("임시 포스트 생성 또는 조회 - 임시 포스트가 있는 경우")
	void findOrCreateTempPost_findExistingTemp_success() {
		// given
		Post tempPost = Post.builder()
			.id(3L)
			.title("임시제목")
			.content("임시내용")
			.user(user1)
			.status(PostStatus.TEMP)
			.build();

		when(userService.findUserByEmail("이메일1")).thenReturn(user1);
		when(postRepository.findFirstByUserAndStatus(user1, PostStatus.TEMP))
			.thenReturn(Optional.of(tempPost));

		// when
		PostTempResponseDto response = postService.findOrCreateTempPost("이메일1");

		// then
		assertEquals(3L, response.id());
		assertEquals("임시제목", response.title());
		assertEquals("임시내용", response.content());
		verify(postRepository, never()).save(any(Post.class));
	}

	@Test
	@DisplayName("임시 포스트 생성 또는 조회 - 임시 포스트가 없는 경우 생성")
	void findOrCreateTempPost_createNewTemp_success() {
		// given
		Post newTempPost = Post.createTempPost(user1);
		ReflectionTestUtils.setField(newTempPost, "id", 3L);

		when(userService.findUserByEmail("이메일1")).thenReturn(user1);
		when(postRepository.findFirstByUserAndStatus(user1, PostStatus.TEMP))
			.thenReturn(Optional.empty());
		when(postRepository.save(any(Post.class))).thenReturn(newTempPost);

		// when
		PostTempResponseDto response = postService.findOrCreateTempPost("이메일1");

		// then
		assertEquals(3L, response.id());
		verify(postRepository, times(1)).save(any(Post.class));
	}

	@Test
	@DisplayName("포스트 내용에서 이미지 URL 추출 - 성공")
	void extractImageUrlsFromContent_success() throws Exception {
		// 비공개 메서드 테스트를 위해 리플렉션 사용
		String content = "이미지 테스트 ![](https://example.com/image1.jpg) 추가 이미지 ![](https://example.com/image2.png)";

		Method method = PostService.class.getDeclaredMethod("extractImageUrlsFromContent", String.class);
		method.setAccessible(true);

		@SuppressWarnings("unchecked")
		Set<String> urls = (Set<String>)method.invoke(postService, content);

		assertEquals(2, urls.size());
		assertTrue(urls.contains("https://example.com/image1.jpg"));
		assertTrue(urls.contains("https://example.com/image2.png"));
	}

	@Test
	@DisplayName("포스트 단건 조회 - 로그인 한 경우 성공")
	void getPost_withLoggedInUser_success() {
		// given
		when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
		when(userService.findUserByEmail(user1.getEmail())).thenReturn(user1);
		when(likeService.getLikeStatus(user1.getId(), 1L, TargetType.POST)).thenReturn(true);

		// when
		PostDetailResponseDto dto = postService.getPost(user1.getEmail(), 1L);

		// then
		assertEquals(1L, dto.id());
		assertEquals("제목1", dto.title());
		assertEquals("내용1", dto.content());
		assertTrue(dto.likeStatus());
		verify(likeService, times(1)).getLikeStatus(anyLong(), anyLong(), any());
	}

	@Test
	@DisplayName("포스트 단건 조회 - 로그인 안 한 경우 성공")
	void getPost_withNoUser_success() {
		// given
		when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));

		// when
		PostDetailResponseDto dto = postService.getPost(null, 1L); // 비로그인 사용자

		// then
		assertEquals(1L, dto.id());
		assertEquals("제목1", dto.title());
		assertEquals("내용1", dto.content());
		assertFalse(dto.likeStatus());
		verify(likeService, never()).getLikeStatus(anyLong(), anyLong(), any());
	}

	@Test
	@DisplayName("포스트 단건 조회 - 로그인하고 좋아요하지 않은 경우 성공")
	void getPost_withLoggedInUserNotLiked_success() {
		// given
		when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
		when(userService.findUserByEmail(user1.getEmail())).thenReturn(user1);
		when(likeService.getLikeStatus(user1.getId(), 1L, TargetType.POST)).thenReturn(false);

		// when
		PostDetailResponseDto dto = postService.getPost(user1.getEmail(), 1L);

		// then
		assertEquals(1L, dto.id());
		assertEquals("제목1", dto.title());
		assertEquals("내용1", dto.content());
		assertFalse(dto.likeStatus());
		verify(likeService, times(1)).getLikeStatus(anyLong(), anyLong(), any());
	}
}
