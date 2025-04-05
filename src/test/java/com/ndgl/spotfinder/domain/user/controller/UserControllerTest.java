package com.ndgl.spotfinder.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.domain.user.dto.UserJoinRequest;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.service.OauthService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@InjectMocks
	private OauthService oauthService;

	@Mock
	private RestTemplate restTemplate;

	@Test
	@DisplayName("로그인 테스트")
	void login_success() throws Exception {
		mockMvc.perform(get("/api/v1/users/google/login/process")
				.param("code", "dummy_code")
				.param("redirect_uri", "http://localhost:3000/callback/google"))
			.andExpect(status().isOk());
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
