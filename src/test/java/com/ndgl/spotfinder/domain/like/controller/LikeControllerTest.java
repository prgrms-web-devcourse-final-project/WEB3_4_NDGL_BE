// package com.ndgl.spotfinder.domain.like.controller;
//
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.test.context.support.WithUserDetails;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.ResultActions;
// import org.springframework.transaction.annotation.Transactional;
//
// import com.ndgl.spotfinder.domain.comment.entity.PostComment;
// import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
// import com.ndgl.spotfinder.domain.post.entity.Post;
// import com.ndgl.spotfinder.domain.post.repository.PostRepository;
// import com.ndgl.spotfinder.domain.user.entity.User;
// import com.ndgl.spotfinder.domain.user.repository.UserRepository;
//
// /**
//  * @see LikeController
//  */
// @SpringBootTest
// @ActiveProfiles("test")
// @AutoConfigureMockMvc
// @Transactional
// class LikeControllerTest {
//
// 	@Autowired
// 	private MockMvc mvc;
//
// 	@Autowired
// 	private UserRepository userRepository;
//
// 	@Autowired
// 	private PostRepository postRepository;
//
// 	@Autowired
// 	private PostCommentRepository postCommentRepository;
//
// 	private User testUser;
// 	private Post testPost;
// 	private PostComment testComment;
//
// 	@BeforeEach
// 	void setUp() {
// 		testUser = User.builder()
// 			.email("test@example.com")
// 			.nickName("testUser")
// 			.blogName("testBlog")
// 			.build();
// 		testUser = userRepository.save(testUser);
//
// 		testPost = Post.builder()
// 			.title("Test Post")
// 			.content("Test Content")
// 			.user(testUser)
// 			.thumbnail("http://example.com/thumbnail.jpg")
// 			.viewCount(0L)
// 			.likeCount(0L)
// 			.build();
// 		testPost = postRepository.save(testPost);
//
// 		testComment = PostComment.builder()
// 			.content("Test Comment")
// 			.user(testUser)
// 			.post(testPost)
// 			.likeCount(0L)
// 			.build();
// 		testComment = postCommentRepository.save(testComment);
// 	}
//
// 	@Test
// 	@DisplayName("포스트 좋아요 추가")
// 	@WithUserDetails("admin")
// 	void addPostLikeTest() throws Exception {
// 		ResultActions resultActions = mvc
// 			.perform(
// 				post("/api/v1/like/posts/" + testPost.getId())
// 			)
// 			.andDo(print());
//
// 		resultActions
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.message").value("OK"))
// 			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
// 			.andExpect(jsonPath("$.data").value(true));
// 	}
//
// 	@Test
// 	@DisplayName("좋아요 추가에 음수 입력시")
// 	void addPostLikeTest2() throws Exception {
// 		ResultActions resultActions = mvc
// 			.perform(
// 				post("/api/v1/like/posts/" + -20)
// 			)
// 			.andDo(print());
//
// 		resultActions
// 			.andExpect(status().isNotFound())
// 			.andExpect(jsonPath("$.message").value("Not Found"))
// 			.andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()));
// 	}
//
// 	@Test
// 	@DisplayName("포스트 좋아요 취소")
// 	void cancelPostLikeTest() throws Exception {
// 		// 추가
// 		mvc
// 			.perform(
// 				post("/api/v1/like/posts/" + testPost.getId())
// 			)
// 			.andDo(print());
//
// 		ResultActions resultActions = mvc
// 			.perform(
// 				post("/api/v1/like/posts/" + testPost.getId())
// 			)
// 			.andDo(print());
//
// 		resultActions
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.message").value("OK"))
// 			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
// 			.andExpect(jsonPath("$.data").value(false));
// 	}
//
// 	@Test
// 	@DisplayName("댓글 좋아요 추가")
// 	void addCommentLikeTest() throws Exception {
// 		ResultActions resultActions = mvc
// 			.perform(
// 				post("/api/v1/like/comments/" + testComment.getId())
// 			)
// 			.andDo(print());
//
// 		resultActions
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.message").value("OK"))
// 			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
// 			.andExpect(jsonPath("$.data").value(true));
// 	}
//
// 	@Test
// 	@DisplayName("댓글 좋아요 취소")
// 	void cancelCommentLikeTest() throws Exception {
// 		// 추가
// 		mvc
// 			.perform(
// 				post("/api/v1/like/comments/" + testComment.getId())
// 			)
// 			.andDo(print());
//
// 		ResultActions resultActions = mvc
// 			.perform(
// 				post("/api/v1/like/comments/" + testComment.getId())
// 			)
// 			.andDo(print());
//
// 		resultActions
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.message").value("OK"))
// 			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
// 			.andExpect(jsonPath("$.data").value(false));
// 	}
//
// }