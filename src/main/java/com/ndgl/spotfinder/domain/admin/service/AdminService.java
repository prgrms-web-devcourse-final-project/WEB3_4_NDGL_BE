package com.ndgl.spotfinder.domain.admin.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.admin.dto.CreateAdminRequest;
import com.ndgl.spotfinder.domain.admin.entity.Admin;
import com.ndgl.spotfinder.domain.admin.repository.AdminRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;

	public void join(CreateAdminRequest createAdminRequest) {
		Admin admin = Admin.builder()
			.username(createAdminRequest.username())
			.password(passwordEncoder.encode(createAdminRequest.password()))
			.build();

		adminRepository.save(admin);
	}

	public void resign(String username) {
		Admin admin = findAdminByUsername(username);

		adminRepository.delete(admin);
	}

	public Admin findAdminByUsername(String username) {
		return adminRepository.findByUsername(username)
			.orElseThrow(ErrorCode.ADMIN_NOT_FOUND::throwServiceException);
	}
}
