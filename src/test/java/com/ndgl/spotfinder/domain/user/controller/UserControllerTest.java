package com.ndgl.spotfinder.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.domain.user.dto.UserJoinRequest;
import com.ndgl.spotfinder.domain.user.entity.Oauth;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("secret")
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("Google 로그인 요청 시 302 리다이렉트 요청 확인")
	void loginRedirectsToGoogle() throws Exception {
		mockMvc.perform(get("/api/v1/users/google/login"))
			.andExpect(status().isFound()) // 302
			.andExpect(header().string("Location",
				org.hamcrest.Matchers.containsString("https://accounts.google.com/o/oauth2/auth")));
	}

	@Test
	@DisplayName("회원가입 테스트")
	void join_success() throws Exception {
		UserJoinRequest request = UserJoinRequest.builder()
			.provider(Oauth.Provider.GOOGLE)
			.identify("123456789")
			.email("testman001@gmail.com")
			.nickName("testman001")
			.blogName("testblog001")
			.build();

		mockMvc.perform(post("/api/v1/users/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("OK"));
	}
}
