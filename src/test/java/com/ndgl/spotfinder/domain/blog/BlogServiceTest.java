package com.ndgl.spotfinder.domain.blog;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.blog.dto.BlogResponseDto;
import com.ndgl.spotfinder.domain.blog.dto.PostSummeryDto;
import com.ndgl.spotfinder.domain.blog.service.BlogService;
import com.ndgl.spotfinder.domain.follow.service.FollowService;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;

@ActiveProfiles("test")
@SpringBootTest
class BlogServiceTest {
	@InjectMocks
	private BlogService blogService;

	@Mock
	private UserService userService;

	@Mock
	private PostService postService;

	@Mock
	private FollowService followService;

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

	private final User user3 = User.builder()
		.id(3L)
		.email("이메일3")
		.nickName("별명3")
		.blogName("블로그3")
		.build();

	private final Post samplePost1 = Post.builder()
		.id(1L)
		.title("제목1")
		.content("내용1")
		.user(user2)
		.build();

	private final Post samplePost2 = Post.builder()
		.id(2L)
		.title("제목2")
		.content("내용2")
		.user(user3)
		.build();

	@Test
	@DisplayName("블로그 목록 조회 - 성공")
	void getBlogs_success() {
		// given
		SliceRequest sliceRequest = new SliceRequest(4L, 3);
		String email = "이메일1";
		Slice<User> users = new SliceImpl<>(List.of(user2, user3));

		// when
		when(userService.findUserByEmail(email)).thenReturn(user1);
		when(userService.findUsers(sliceRequest)).thenReturn(users);
		when(followService.isFollowed(user1, user2)).thenReturn(true);
		when(followService.isFollowed(user1, user3)).thenReturn(false);
		when(postService.getPostsByUser(2L)).thenReturn(List.of(samplePost1));
		when(postService.getPostsByUser(3L)).thenReturn(List.of(samplePost2));

		// then
		BlogResponseDto dto1 = new BlogResponseDto(
			2L,
			"블로그2",
			"별명2",
			true,
			List.of(new PostSummeryDto(1L, "제목1"))
		);

		BlogResponseDto dto2 = new BlogResponseDto(
			3L,
			"블로그3",
			"별명3",
			false,
			List.of(new PostSummeryDto(2L, "제목2"))
		);

		SliceResponse<BlogResponseDto> expectedResult = new SliceResponse<>(
			List.of(dto1, dto2),
			false
		);

		assertThat(blogService.getBlogs(sliceRequest, email))
			.isEqualTo(expectedResult);
	}
}
