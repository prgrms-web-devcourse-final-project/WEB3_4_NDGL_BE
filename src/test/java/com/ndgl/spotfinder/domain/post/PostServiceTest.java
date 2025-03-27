package com.ndgl.spotfinder.domain.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.post.dto.HashtagDto;
import com.ndgl.spotfinder.domain.post.dto.LocationDto;
import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;

@ActiveProfiles("test")
@SpringBootTest
public class PostServiceTest {
	@InjectMocks
	private PostService postService;

	@Mock
	private PostRepository postRepository;

	@Test
	public void createPost_success() {
		// given
		HashtagDto hashtagDto = new HashtagDto("태그1");
		LocationDto locationDto = new LocationDto(
			"장소1",
			"주소1",
			37.0,
			126.0,
			1
		);
		PostCreateRequestDto postDto = new PostCreateRequestDto(
			"제목1",
			"내용1",
			List.of(hashtagDto),
			List.of(locationDto)
		);

		// when
		postService.createPost(postDto);

		// then
		verify(postRepository, times(1)).save(any());

		ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
		verify(postRepository).save(postCaptor.capture());

		Post savedPost = postCaptor.getValue();
		assertEquals("제목1", savedPost.getTitle());
		assertEquals("내용1", savedPost.getContent());
		assertEquals(0, savedPost.getViewCount());
		assertEquals(0, savedPost.getLikeCount());
		assertEquals(1, savedPost.getHashtags().size());
		assertEquals(1, savedPost.getLocations().size());
	}
}
