package com.ndgl.spotfinder.domain.like.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.like.service.LikeService;

/**
 * TODO like, user 연관관계 설정 시 userId 수정 필요 검토
 *
 * @see LikeController
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class LikeControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private LikeService likeService;

	private final Integer userId = 1;
	private final Integer postId = 100;
	private final Integer commentId = 200;

	@Test
	@DisplayName("포스트 좋아요 추가")
	void addPostLikeTest() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/like/posts/" + postId)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.data").value(true));

	}

	@Test
	@DisplayName("좋아요 추가에 음수 입력시")
	void addPostLikeTest2() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/like/posts/" + -20)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Not Found"))
			.andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()));

	}

	@Test
	@DisplayName("포스트 좋아요 취소")
	void cancelPostLikeTest() throws Exception {
		// 추가
		mvc
			.perform(
				post("/api/v1/like/posts/" + postId)
			)
			.andDo(print());

		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/like/posts/" + postId)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.data").value(false));
	}

	@Test
	@DisplayName("댓글 좋아요 추가")
	void addCommentLikeTest() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/like/comments/" + commentId)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.data").value(true));
	}

	@Test
	@DisplayName("댓글 좋아요 취소")
	void cancelCommentLikeTest() throws Exception {
		// 추가
		mvc
			.perform(
				post("/api/v1/like/comments/" + commentId)
			)
			.andDo(print());

		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/like/comments/" + commentId)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.data").value(false));
	}

}