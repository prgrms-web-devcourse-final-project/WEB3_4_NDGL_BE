package com.ndgl.spotfinder.global.security.jwt.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.admin.entity.Admin;
import com.ndgl.spotfinder.domain.admin.repository.AdminRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.security.jwt.AdminUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {
	private final AdminRepository adminRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Admin admin = adminRepository.findByUsername(username)
			.orElseThrow(ErrorCode.ADMIN_NOT_FOUND::throwServiceException);

		return new AdminUserDetails(admin);
	}
}
