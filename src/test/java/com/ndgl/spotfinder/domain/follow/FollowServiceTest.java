package com.ndgl.spotfinder.domain.follow;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.follow.entity.Follow;
import com.ndgl.spotfinder.domain.follow.repository.FollowRepository;
import com.ndgl.spotfinder.domain.follow.service.FollowService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ActiveProfiles("test")
@SpringBootTest
public class FollowServiceTest {
	@InjectMocks
	private FollowService followService;

	@Mock
	private FollowRepository followRepository;

	@Mock
	private UserService userService;

	private final User user1 = User.builder()
		.id(1L)
		.email("user1@example.com")
		.build();

	private final User user2 = User.builder()
		.id(2L)
		.email("user2@example.com")
		.build();

	@Test
	public void createFollow_success() {
		// when
		when(userService.findUserByEmail("user1@example.com")).thenReturn(user1);
		when(userService.findUserById(2L)).thenReturn(user2);
		when(followService.isFollowed(user1, user2)).thenReturn(false);

		followService.createFollow("user1@example.com", 2L);

		// then
		verify(followRepository, times(1)).save(any(Follow.class));
	}

	@Test
	public void createFollow_followerEqualsFollowee() {
		// when
		when(userService.findUserByEmail("user1@example.com")).thenReturn(user1);
		when(userService.findUserById(1L)).thenReturn(user1);

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> followService.createFollow("user1@example.com", 1L));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
	}

	@Test
	public void createFollow_alreadyFollowing() {
		// when
		when(userService.findUserByEmail("user1@example.com")).thenReturn(user1);
		when(userService.findUserById(2L)).thenReturn(user2);
		when(followService.isFollowed(user1, user2)).thenReturn(true);

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> followService.createFollow("user1@example.com", 2L));
		assertEquals(HttpStatus.CONFLICT, exception.getCode());
	}

	@Test
	public void deleteFollow_success() {
		// when
		when(userService.findUserByEmail("user1@example.com")).thenReturn(user1);
		when(userService.findUserById(2L)).thenReturn(user2);
		when(followService.isFollowed(user1, user2)).thenReturn(true);

		followService.deleteFollow("user1@example.com", 2L);

		// then
		verify(followRepository, times(1)).deleteFollowByFollowerAndFollowee(user1, user2);
	}

	@Test
	public void deleteFollow_followerEqualsFollowee() {
		// when
		when(userService.findUserByEmail("user1@example.com")).thenReturn(user1);
		when(userService.findUserById(1L)).thenReturn(user1);

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> followService.deleteFollow("user1@example.com", 1L));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
	}

	@Test
	public void deleteFollow_notFollowing() {
		// when
		when(userService.findUserByEmail("user1@example.com")).thenReturn(user1);
		when(userService.findUserById(2L)).thenReturn(user2);
		when(followService.isFollowed(user1, user2)).thenReturn(false);

		// then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> followService.deleteFollow("user1@example.com", 2L));
		assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
	}
}
