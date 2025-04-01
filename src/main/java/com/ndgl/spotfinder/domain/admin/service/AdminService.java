package com.ndgl.spotfinder.domain.admin.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.admin.dto.CreateAdminRequest;
import com.ndgl.spotfinder.domain.admin.entity.Admin;
import com.ndgl.spotfinder.domain.admin.repository.AdminRepository;

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
}
