package com.ndgl.spotfinder.domain.user.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public Slice<User> findUsers(SliceRequest sliceRequest) {
		PageRequest pageRequest = PageRequest.of(0, sliceRequest.size());
		Long lastId = sliceRequest.lastId() == null ? 0 : sliceRequest.lastId();

		return userRepository.findAllByIdGreaterThan(lastId, pageRequest);
	}

	public User findUserById(long userId) {
		return userRepository.findById(userId)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);
	}

	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);
	}
}
