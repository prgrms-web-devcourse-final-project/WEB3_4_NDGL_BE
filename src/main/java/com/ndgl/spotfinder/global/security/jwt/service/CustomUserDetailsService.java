package com.ndgl.spotfinder.global.security.jwt.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.security.jwt.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String nickName) throws UsernameNotFoundException {
		User user = userRepository.findByNickName(nickName)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);

		return new CustomUserDetails(user);
	}
}
