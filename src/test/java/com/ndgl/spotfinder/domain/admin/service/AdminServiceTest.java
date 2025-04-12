package com.ndgl.spotfinder.domain.admin.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ndgl.spotfinder.domain.admin.dto.CreateAdminRequest;
import com.ndgl.spotfinder.domain.admin.entity.Admin;
import com.ndgl.spotfinder.domain.admin.repository.AdminRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

	@Mock
	private AdminRepository adminRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AdminService adminService;

	private final String USERNAME = "testAdmin";
	private final String PASSWORD = "password123";
	private final String ENCODED_PASSWORD = "encodedPassword123";

	@Test
	@DisplayName("관리자 회원가입 - 정상")
	void 정상_관리자_회원가입() {
		// given

		CreateAdminRequest createAdminRequest = new CreateAdminRequest(USERNAME, PASSWORD);

		Admin admin = Admin.builder()
			.username(USERNAME)
			.password(ENCODED_PASSWORD)
			.build();

		Admin savedAdmin = spy(admin);
		when(savedAdmin.getId()).thenReturn(1L);
		when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
		when(adminRepository.save(any(Admin.class))).thenReturn(savedAdmin);

		// when
		adminService.join(createAdminRequest);

		// then
		verify(passwordEncoder).encode(PASSWORD);
		verify(adminRepository).save(any(Admin.class));
	}

	@Test
	@DisplayName("관리자 회원가입 - username 중복")
	void 비정상_관리자_회원가입_username_중복() {
		// given
		CreateAdminRequest duplicateRequest = new CreateAdminRequest(USERNAME, PASSWORD);
		when(adminRepository.existsAdminByUsername(anyString())).thenReturn(true);

		// when & then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> adminService.join(duplicateRequest));

		assertThat(exception.getCode()).isEqualTo(ErrorCode.ADMIN_ALREADY_EXISTS_USERNAME.getHttpStatus());
		assertThat(exception.getMessage()).isEqualTo(ErrorCode.ADMIN_ALREADY_EXISTS_USERNAME.getMessage());
		verify(adminRepository).existsAdminByUsername(duplicateRequest.username());
	}

	@Test
	@DisplayName("관리자 계정 탈퇴 - 정상")
	void 정상_관리자_탈퇴() {
		// given
		Admin admin = Admin.builder()
			.username(USERNAME)
			.password(ENCODED_PASSWORD)
			.build();
		when(adminRepository.findByUsername(USERNAME)).thenReturn(Optional.of(admin));

		// when
		adminService.resign(USERNAME);

		// then
		verify(adminRepository).findByUsername(USERNAME);
		verify(adminRepository).delete(admin);
	}

	@Test
	@DisplayName("관리자 계정 탈퇴 - 관리자 존재 X")
	void 비정상_관리자_탈퇴_관리자_존재_X() {
		// given
		when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());

		// when & then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> adminService.resign("nonExistentAdmin"));

		assertThat(exception.getCode()).isEqualTo(ErrorCode.ADMIN_NOT_FOUND.getHttpStatus());
		assertThat(exception.getMessage()).isEqualTo(ErrorCode.ADMIN_NOT_FOUND.getMessage());
		verify(adminRepository).findByUsername("nonExistentAdmin");
	}

	@Test
	@DisplayName("username 로 관리자 찾기 - 정상")
	void 정상_username으로_관리자_찾기() {
		// given
		Admin admin = Admin.builder()
			.username(USERNAME)
			.password(ENCODED_PASSWORD)
			.build();

		when(adminRepository.findByUsername(USERNAME)).thenReturn(Optional.of(admin));

		// when
		Admin foundAdmin = adminService.findAdminByUsername(USERNAME);

		// then
		assertThat(foundAdmin).isNotNull();
		assertThat(foundAdmin.getUsername()).isEqualTo(USERNAME);
		assertThat(foundAdmin.getPassword()).isEqualTo(ENCODED_PASSWORD);
		verify(adminRepository).findByUsername(USERNAME);
	}

	@Test
	@DisplayName("username 로 관리자 찾기 - 관리자 존재 X")
	void 비정상_username으로_관리자_찾기_관리자_존재_X() {
		// given
		when(adminRepository.findByUsername(anyString())).thenReturn(Optional.empty());

		// when & then
		ServiceException exception = assertThrows(ServiceException.class,
			() -> adminService.findAdminByUsername("nonExistentAdmin"));

		assertThat(exception.getCode()).isEqualTo(ErrorCode.ADMIN_NOT_FOUND.getHttpStatus());
		assertThat(exception.getMessage()).isEqualTo(ErrorCode.ADMIN_NOT_FOUND.getMessage());
		verify(adminRepository).findByUsername("nonExistentAdmin");
	}
}
