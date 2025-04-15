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
import com.ndgl.spotfinder.domain.user.dto.UserJoinRequestDto;
import com.ndgl.spotfinder.domain.user.type.Provider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("회원가입 성공 테스트")
	void join_success() throws Exception {
		UserJoinRequestDto request = UserJoinRequestDto.builder()
			.provider(Provider.GOOGLE)
			.identify("123456790")
			.email("testman004@gmail.com")
			.nickName("testman004")
			.blogName("testblog004")
			.build();

		mockMvc.perform(post("/api/v1/users/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("OK"));
	}

	@Test
	@DisplayName("회원가입 실패 테스트 - 동일 닉네임")
	void join_sameNickname_fail() throws Exception {
		UserJoinRequestDto request = UserJoinRequestDto.builder()
			.provider(Provider.GOOGLE)
			.identify("123456791")
			.email("testman005@gmail.com")
			.nickName("testman004")
			.blogName("testblog005")
			.build();

		mockMvc.perform(post("/api/v1/users/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value(409))
			.andExpect(jsonPath("$.message").value("이미 사용중인 닉네임 입니다."));
	}

	@Test
	@DisplayName("회원가입 실패 테스트 - 동일 블로그 명")
	void join_sameBlogName_fail() throws Exception {
		UserJoinRequestDto request = UserJoinRequestDto.builder()
			.provider(Provider.GOOGLE)
			.identify("123456792")
			.email("testman006@gmail.com")
			.nickName("testman005")
			.blogName("testblog004")
			.build();

		mockMvc.perform(post("/api/v1/users/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value(409))
			.andExpect(jsonPath("$.message").value("이미 사용중인 블로그 명 입니다."));
	}

	@Test
	@DisplayName("회원가입 실패 테스트 - 닉네임 공백")
	void join_blankNickName_fail() throws Exception {
		UserJoinRequestDto request = UserJoinRequestDto.builder()
			.provider(Provider.GOOGLE)
			.identify("123456793")
			.email("testman007@gmail.com")
			.nickName(null)
			.blogName("testblog005")
			.build();

		mockMvc.perform(post("/api/v1/users/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(400))
			.andExpect(jsonPath("$.message").value("nickName 값이 필요합니다."));
	}

	@Test
	@DisplayName("회원가입 실패 테스트 - 블로그 명 공백")
	void join_blankBlogName_fail() throws Exception {
		UserJoinRequestDto request = UserJoinRequestDto.builder()
			.provider(Provider.GOOGLE)
			.identify("123456794")
			.email("testman008@gmail.com")
			.nickName("testman005")
			.blogName(null)
			.build();

		mockMvc.perform(post("/api/v1/users/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(400))
			.andExpect(jsonPath("$.message").value("blogName 값이 필요합니다."));
	}
}
