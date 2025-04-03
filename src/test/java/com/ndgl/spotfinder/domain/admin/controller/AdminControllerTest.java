package com.ndgl.spotfinder.domain.admin.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.ndgl.spotfinder.domain.admin.dto.CreateAdminRequest;
import com.ndgl.spotfinder.domain.admin.entity.Admin;
import com.ndgl.spotfinder.domain.admin.repository.AdminRepository;
import com.ndgl.spotfinder.domain.admin.service.AdminService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;
import com.ndgl.spotfinder.global.security.jwt.AdminUserDetails;
import com.ndgl.spotfinder.global.security.jwt.CustomUserDetails;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class AdminControllerTest {
	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AdminService adminService;

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private UserService userService;


	void setUpUserAuth(long userId) {
		User user = userService.findUserById(userId);
		CustomUserDetails userDetails = new CustomUserDetails(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	void setUpAdminAuth(String adminUsername) {
		Admin admin = adminService.findAdminByUsername(adminUsername);
		AdminUserDetails adminDetails = new AdminUserDetails(admin);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			adminDetails, null, adminDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	@DisplayName("관리자 생성 - 정상")
	void 정상_관리자_생성() throws Exception {

		String username = "admin1";
		String password = "admin123";

		CreateAdminRequest createAdminRequest = new CreateAdminRequest(username, password);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/join")
				.content(objectMapper.writeValueAsString(createAdminRequest))
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
		);

		resultActions
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("joinAdmin"))
			.andExpect(status().isOk());

		// Admin 의 id 값 API 응답에서 추출
		MvcResult mvcResult = resultActions.andReturn();
		String responseJson = mvcResult.getResponse().getContentAsString();

		Integer id = JsonPath.read(responseJson, "$.data.id");
		Admin admin = adminRepository.findById(Long.valueOf(id))
			.orElseThrow(ErrorCode.ADMIN_NOT_FOUND::throwServiceException);

		assertThat(admin.getUsername()).isEqualTo(username);
	}

	@Test
	@DisplayName("관리자 생성 - username 중복")
	void 비정상_관리자_생성_username_중복() throws Exception {

		String username = "admin";
		String password = "admin123";

		CreateAdminRequest createAdminRequest = new CreateAdminRequest(username, password);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/join")
				.content(objectMapper.writeValueAsString(createAdminRequest))
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
		);

		resultActions
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("joinAdmin"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value(ErrorCode.ADMIN_ALREADY_EXISTS_USERNAME.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ADMIN_ALREADY_EXISTS_USERNAME.getMessage()));
	}

	@Test
	@DisplayName("관리자 생성 - 비정상 username (4자 미만")
	void 비정상_관리자_생성_비정상_username_4자_미만() throws Exception {

		String invalidUsername = "ad1";
		String password = "admin123";

		CreateAdminRequest createAdminRequest = new CreateAdminRequest(invalidUsername, password);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/join")
				.content(objectMapper.writeValueAsString(createAdminRequest))
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
		);

		resultActions
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("joinAdmin"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("사용자 이름은 4자 이상 20자 이하여야 합니다."));
	}

	@Test
	@DisplayName("관리자 생성 - username 데이터 누락")
	void 비정상_관리자_생성_username_데이터_누락() throws Exception {

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/join")
				.content("""
					{
						"username" : "",
						"password" : "admin123"
					}
					""")
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
		);

		resultActions
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("joinAdmin"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message",containsString("사용자 이름은 필수 입력 항목입니다.")));
	}

	@Test
	@DisplayName("관리자 생성 - 비정상 password (4자 미만")
	void 비정상_관리자_생성_비정상_password_4자_미만() throws Exception {

		String username = "admin1";
		String invalidPassword = "ad1";

		CreateAdminRequest createAdminRequest = new CreateAdminRequest(username, invalidPassword);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/join")
				.content(objectMapper.writeValueAsString(createAdminRequest))
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
		);

		resultActions
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("joinAdmin"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("비밀번호는 4자 이상 20자 이하여야 합니다."));
	}

	@Test
	@DisplayName("관리자 생성 - password 데이터 누락")
	void 비정상_관리자_생성_password_데이터_누락() throws Exception {

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/join")
				.content("""
					{
						"username" : "admin1",
						"password" : ""
					}
					""")
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
		);

		resultActions
			.andExpect(handler().handlerType(AdminController.class))
			.andExpect(handler().methodName("joinAdmin"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message", containsString("비밀번호는 필수 입력 항목입니다.")));
	}

	@Test
	@DisplayName("관리자 로그인 - 정상")
	void 정상_관리자_로그인() throws Exception {

		String username = "admin";
		String password = "12345";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/login")
				.param("username", username)
				.param("password", password)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		);

		resultActions
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("관리자 로그인 - 비정상 username")
	void 비정상_관리자_로그인_비정상_username() throws Exception {

		String username = "invalid";
		String password = "12345";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/login")
				.param("username", username)
				.param("password", password)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		);

		resultActions
			.andExpect(status().isFound())
			.andExpect(redirectedUrlPattern("/login?error"));
	}

	@Test
	@DisplayName("관리자 로그인 - 비정상 password")
	void 비정상_관리자_로그인_비정상_password() throws Exception {

		String username = "admin";
		String password = "invalid";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/login")
				.param("username", username)
				.param("password", password)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		);

		resultActions
			.andExpect(status().isFound())
			.andExpect(redirectedUrlPattern("/login?error"));
	}

	@Test
	@DisplayName("관리자 체크 - 정상")
	void 정상_관리자_체크() throws Exception {

		setUpAdminAuth("admin");

		ResultActions resultActions = mvc.perform(
			get("/api/v1/admin/posts/statistics"));

		resultActions
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("관리자 체크 - 권한 X")
	void 비정상_관리자_체크_권한_X() throws Exception {

		setUpUserAuth(1L);

		ResultActions resultActions = mvc.perform(
			get("/api/v1/admin/posts/statistics"));

		resultActions
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
	}

	@Test
	@DisplayName("관리자 체크 - 인증 X")
	void 비정상_관리자_체크_인증_X() throws Exception {

		ResultActions resultActions = mvc.perform(
			get("/api/v1/admin/posts/statistics"));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("관리자 로그아웃 - 정상")
	void 정상_관리자_로그아웃() throws Exception {

		String adminUsername = "admin";
		setUpAdminAuth(adminUsername);

		ResultActions resultActions = mvc.perform(
			get("/api/v1/admin/posts/statistics"));

		resultActions
			.andExpect(status().isOk());

		resultActions = mvc.perform(
			post("/api/v1/admin/logout"));

		resultActions
			.andExpect(status().isOk());

		resultActions = mvc.perform(
			get("/api/v1/admin/posts/statistics"));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("관리자 탈퇴 - 정상")
	void 정상_관리자_탈퇴() throws Exception {

		String adminUsername = "admin";
		setUpAdminAuth(adminUsername);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/resign"));

		resultActions
			.andExpect(status().isOk());

		assertThatThrownBy(() -> adminService.findAdminByUsername(adminUsername))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(ErrorCode.ADMIN_NOT_FOUND.getHttpStatus());
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.ADMIN_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("관리자 탈퇴 - 권한 X")
	void 비정상_관리자_탈퇴_권한_X() throws Exception {

		setUpUserAuth(1L);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/admin/resign"));

		resultActions
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
	}

	@Test
	@DisplayName("관리자 탈퇴 - 인증 X")
	void 비정상_관리자_탈퇴_인증_X() throws Exception {

		ResultActions resultActions = mvc.perform(
			get("/api/v1/admin/resign"));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}
}
