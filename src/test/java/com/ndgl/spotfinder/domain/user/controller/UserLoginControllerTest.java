package com.ndgl.spotfinder.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserLoginControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("Google 로그인 요청 시 302 리다이렉트 요청 확인")
	void loginRedirectsToGoogle() throws Exception {
		mockMvc.perform(get("/api/v1/user/login"))
			.andExpect(status().isFound()) // 302
			.andExpect(header().string("Location",
				org.hamcrest.Matchers.containsString("https://accounts.google.com/o/oauth2/auth")));
	}
}
