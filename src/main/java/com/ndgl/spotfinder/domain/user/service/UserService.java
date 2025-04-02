package com.ndgl.spotfinder.domain.user.service;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public User findUserById(long userId) {
		return userRepository.findById(userId)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);
	}

	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);
	}
}
