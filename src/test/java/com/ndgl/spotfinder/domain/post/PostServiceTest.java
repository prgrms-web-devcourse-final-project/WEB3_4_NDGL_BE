package com.ndgl.spotfinder.domain.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.post.dto.HashtagDto;
import com.ndgl.spotfinder.domain.post.dto.LocationDto;
import com.ndgl.spotfinder.domain.post.dto.PostCreateRequestDto;
import com.ndgl.spotfinder.domain.post.dto.PostUpdateRequestDto;
import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Location;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ActiveProfiles("test")
@SpringBootTest
public class PostServiceTest {
	@InjectMocks
	private PostService postService;

	@Mock
	private PostRepository postRepository;

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
		.build();

	@BeforeEach
	void setUp() {
		samplePost.addHashtag(samplehashtag);
		samplePost.addLocation(samplelocation);
	}

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
		PostCreateRequestDto requestDto = new PostCreateRequestDto(
			"제목1",
			"내용1",
			List.of(hashtagDto),
			List.of(locationDto)
		);

		// when
		postService.createPost(requestDto);

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

	@Test
	public void updatePost_success() {
		// given
		HashtagDto hashtagDto = new HashtagDto("태그2");
		LocationDto locationDto = new LocationDto(
			"장소2",
			"주소2",
			35.5,
			126.5,
			1
		);
		PostUpdateRequestDto requestDto = new PostUpdateRequestDto(
			"제목2",
			"내용2",
			List.of(hashtagDto),
			List.of(locationDto)
		);

		// when
		when(postRepository.findById(1L)).thenReturn(Optional.of(samplePost));
		postService.updatePost(1L, requestDto);

		// then
		verify(postRepository, times(1)).save(any());

		ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
		verify(postRepository).save(postCaptor.capture());

		Post savedPost = postCaptor.getValue();
		assertEquals("제목2", savedPost.getTitle());
		assertEquals("내용2", savedPost.getContent());

		Hashtag updatedHashtag = savedPost.getHashtags().get(0);
		Location updatedLocation = savedPost.getLocations().get(0);
		assertEquals("태그2", updatedHashtag.getName());
		assertEquals("장소2", updatedLocation.getName());
		assertEquals("주소2", updatedLocation.getAddress());
		assertEquals(35.5, updatedLocation.getLatitude());
		assertEquals(126.5, updatedLocation.getLongitude());
	}

	@Test
	public void updatePost_notFound() {
		// given
		HashtagDto hashtagDto = new HashtagDto("태그2");
		LocationDto locationDto = new LocationDto(
			"장소2",
			"주소2",
			35.5,
			126.5,
			1
		);
		PostUpdateRequestDto requestDto = new PostUpdateRequestDto(
			"제목2",
			"내용2",
			List.of(hashtagDto),
			List.of(locationDto)
		);

		// when
		when(postRepository.findById(1L)).thenReturn(Optional.empty());

		// then
		ServiceException exception = assertThrows(ServiceException.class, () -> postService.updatePost(1L, requestDto));
		assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
	}
}
