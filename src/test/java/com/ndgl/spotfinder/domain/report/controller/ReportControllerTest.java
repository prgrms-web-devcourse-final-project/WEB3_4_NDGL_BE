package com.ndgl.spotfinder.domain.report.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.domain.admin.entity.Admin;
import com.ndgl.spotfinder.domain.admin.service.AdminService;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.report.dto.PostCommentReportDto;
import com.ndgl.spotfinder.domain.report.dto.PostReportDto;
import com.ndgl.spotfinder.domain.report.dto.ReportCreateRequest;
import com.ndgl.spotfinder.domain.report.entity.PostCommentReport;
import com.ndgl.spotfinder.domain.report.entity.PostReport;
import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;
import com.ndgl.spotfinder.domain.report.repository.PostCommentReportRepository;
import com.ndgl.spotfinder.domain.report.repository.PostReportRepository;
import com.ndgl.spotfinder.domain.report.service.ReportService;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.security.jwt.AdminUserDetails;
import com.ndgl.spotfinder.global.security.jwt.CustomUserDetails;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class ReportControllerTest {
	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ReportService reportService;

	@Autowired
	private PostReportRepository postReportRepository;

	@Autowired
	private PostCommentReportRepository postCommentReportRepository;

	@Autowired
	private AdminService adminService;

	@Autowired
	private UserService userService;

	@Autowired
	private PostService postService;

	@Autowired
	private PostService postCommentService;


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
	@DisplayName("포스트 신고 요청 - 정상")
	void 정상_포스트_신고_요청() throws Exception {

		long reporterId = 1L;
		long postId = 1L;
		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";

		setUpUserAuth(reporterId);

		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/posts/{id}", postId)
				.content(objectMapper.writeValueAsString(request))
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("createPostReport"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.reporterId").value(reporterId))
			.andExpect(jsonPath("$.data.postId").value(postId))
			.andExpect(jsonPath("$.data.reason").value(reason))
			.andExpect(jsonPath("$.data.reportType").value(reportType.name()));
	}

	@Test
	@DisplayName("포스트 신고 요청 - 비로그인")
	void 비정상_포스트_신고_요청_비로그인() throws Exception {

		long postId = 1L;
		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";

		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/posts/{id}", postId)
				.content(objectMapper.writeValueAsString(request))
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("포스트 신고 요청 - 비정상 요청 데이터")
	void 비정상_포스트_신고_요청_이상한_요청_데이터() throws Exception {

		long reporterId = 1L;
		long postId = 1L;

		setUpUserAuth(reporterId);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/posts/{id}", postId)
				.content("""
					{
					  "reportType": "WRONG",
					  "reason": "이상한 입력 데이터"
					}
				""")
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("createPostReport"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNREADABLE_REQUEST_PAYLOAD.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNREADABLE_REQUEST_PAYLOAD.getMessage()));
	}


	@Test
	@DisplayName("댓글 신고 요청 - 정상")
	void 정상_댓글_신고_요청() throws Exception {

		long reporterId = 1L;
		long postCommentId = 1L;
		ReportType reportType = ReportType.ADVERTISING;
		String reason = "광고 신고";

		setUpUserAuth(reporterId);

		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/comments/{id}", postCommentId)
				.content(objectMapper.writeValueAsString(request))
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("createPostCommentReport"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.reporterId").value(reporterId))
			.andExpect(jsonPath("$.data.postCommentId").value(postCommentId))
			.andExpect(jsonPath("$.data.reason").value(reason))
			.andExpect(jsonPath("$.data.reportType").value(reportType.name()));
	}

	@Test
	@DisplayName("댓글 신고 요청 - 비로그인")
	void 비정상_댓글_신고_요청_비로그인() throws Exception {

		long postId = 1L;
		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";

		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/comments/{id}", postId)
				.content(objectMapper.writeValueAsString(request))
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("댓글 신고 요청 - 비정상 요청 데이터")
	void 비정상_댓글_신고_요청_이상한_요청_데이터() throws Exception {
		long reporterId = 1L;
		long postCommentId = 1L;

		setUpUserAuth(reporterId);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/comments/{id}", postCommentId)
				.content("""
					{
					  "reportType": "WRONG",
					  "reason": "이상한 입력 데이터"
					}
				""")
				.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("createPostCommentReport"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNREADABLE_REQUEST_PAYLOAD.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNREADABLE_REQUEST_PAYLOAD.getMessage()));
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 정상 (10개 이하)")
	void 정상_포스트_신고_목록_조회_10개_이하() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";

		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);

		// 유저 1,2 가 글 1,2,3,4 에 대한 댓글 총 8개 남긴다
		List<PostReportDto> reports = new ArrayList<>();
		for(int i=1; i<3; i++) {
			for (long j = 1; j < 5; j++) {
				reports.add(reportService.createPostReport(request, "test"+i+"@example.com", j));
			}
		}

		long lastId = reports.get(reports.size()-1).id() + 1;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/posts?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostReportList"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.contents", hasSize(8)))
			.andExpect(jsonPath("$.data.hasNext").value(false));
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 정상 (10개 초과)")
	void 정상_포스트_신고_목록_조회_10개_초과() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";

		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);

		// 유저 1,2,3이 글 1,2,3,4에 대한 신고 총 12개 생성
		List<PostReportDto> reports = new ArrayList<>();
		for (int i = 1; i < 4; i++) {
			for (long j = 1; j < 5; j++) {
				reports.add(reportService.createPostReport(request, "test" + i + "@example.com", j));
			}
		}

		// 가장 마지막에 생성된 신고 ID 가져오기
		long lastId = reports.get(reports.size() - 1).id() + 1;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/posts?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostReportList"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.contents", hasSize(10)))
			.andExpect(jsonPath("$.data.hasNext").value(true));
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 데이터 없음")
	void 비정상_포스트_신고_목록_조회_데이터_없음() throws Exception {
		setUpAdminAuth("admin");

		// 가장 마지막에 생성된 신고 ID 가져오기
		long lastId = 0;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/posts?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostReportList"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorCode.EMPTY_POST_REPORT_SLICE.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.EMPTY_POST_REPORT_SLICE.getMessage()));
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 비정상 lastId")
	void 비정상_포스트_신고_목록_조회_비정상_lastId() throws Exception {
		setUpAdminAuth("admin");

		// 가장 마지막에 생성된 신고 ID 가져오기
		long lastId = -1;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/posts?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostReportList"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("요청하는 lastId는 0 이상이어야 합니다."));
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 비정상 size")
	void 비정상_포스트_신고_목록_조회_비정상_size() throws Exception {
		setUpAdminAuth("admin");

		// 가장 마지막에 생성된 신고 ID 가져오기
		long lastId = 0;
		int size = -1;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/posts?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostReportList"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("요청 Slice 사이즈는 양수여야 합니다."));
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 비로그인")
	void 비정상_포스트_신고_목록_조회_비로그인() throws Exception {

		long lastId = 0;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/posts?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 관리자 권한 없음")
	void 비정상_포스트_신고_목록_조회_관리자_권한_없음() throws Exception {
		setUpUserAuth(1L);
		long lastId = 0;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/posts?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
	}


	@Test
	@DisplayName("댓글 신고 목록 조회 - 정상 (10개 이하)")
	void 정상_댓글_신고_목록_조회_10개_이하() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";

		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);

		// 유저 1,2 가 글 1,2,3,4 에 대한 댓글 총 8개 남긴다
		List<PostCommentReportDto> reports = new ArrayList<>();
		for(int i=1; i<3; i++) {
			for (long j = 1; j < 5; j++) {
				reports.add(reportService.createPostCommentReport(request, "test"+i+"@example.com", j));
			}
		}

		long lastId = reports.get(reports.size()-1).id() + 1;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/comments?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostCommentReportList"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.contents", hasSize(8)))
			.andExpect(jsonPath("$.data.hasNext").value(false));
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 정상 (10개 초과)")
	void 정상_댓글_신고_목록_조회_10개_초과() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";

		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);

		// 유저 1,2,3 이 글 1,2,3,4 에 대한 댓글 총 12개 남긴다
		List<PostCommentReportDto> reports = new ArrayList<>();
		for(int i=1; i<4; i++) {
			for (long j = 1; j < 5; j++) {
				reports.add(reportService.createPostCommentReport(request, "test"+i+"@example.com", j));
			}
		}

		long lastId = reports.get(reports.size()-1).id() + 1;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/comments?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostCommentReportList"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.contents", hasSize(10)))
			.andExpect(jsonPath("$.data.hasNext").value(true));
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 데이터 없음")
	void 비정상_댓글_신고_목록_조회_데이터_없음() throws Exception {
		setUpAdminAuth("admin");

		// 가장 마지막에 생성된 신고 ID 가져오기
		long lastId = 0;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/comments?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostCommentReportList"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorCode.EMPTY_COMMENT_REPORT_SLICE.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.EMPTY_COMMENT_REPORT_SLICE.getMessage()));
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 비정상 lastId")
	void 비정상_댓글_신고_목록_조회_비정상_lastId() throws Exception {
		setUpAdminAuth("admin");

		// 가장 마지막에 생성된 신고 ID 가져오기
		long lastId = -1;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/comments?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostCommentReportList"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("요청하는 lastId는 0 이상이어야 합니다."));
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 비정상 size")
	void 비정상_댓글_신고_목록_조회_비정상_size() throws Exception {
		setUpAdminAuth("admin");

		// 가장 마지막에 생성된 신고 ID 가져오기
		long lastId = 0;
		int size = -1;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/comments?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("getPostCommentReportList"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
			.andExpect(jsonPath("$.message").value("요청 Slice 사이즈는 양수여야 합니다."));
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 비로그인")
	void 비정상_댓글_신고_목록_조회_비로그인() throws Exception {

		long lastId = 0;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/comments?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 관리자 권한 없음")
	void 비정상_댓글_신고_목록_조회_관리자_권한_없음() throws Exception {
		setUpUserAuth(1L);
		long lastId = 0;
		int size = 10;

		ResultActions resultActions = mvc.perform(
			get("/api/v1/reports/comments?lastId={lastId}&size={size}", lastId, size));

		resultActions
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
	}

	@Test
	@DisplayName("포스트로 인한 유저 제재 - 정상")
	void 정상_포스트로_인한_유저_제재() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";
		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);
		long postId = 1L;

		PostReportDto postReportDto = reportService.createPostReport(request, "test1@example.com", postId);
		String duration = "7일";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/ban?duration={duration}", postReportDto.id(), duration));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("banUserDueToPost"))
			.andExpect(status().isOk());

		assertThat(postService.findPostById(postId).getUser().isBanned()).isTrue();
	}

	@Test
	@DisplayName("포스트로 인한 유저 제재 - 존재하지 않는 신고")
	void 비정상_포스트로_인한_유저_제재_신고_존재_X() throws Exception {
		setUpAdminAuth("admin");

		long invalidPostReportId = -1L;
		String duration = "7일";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/ban?duration={duration}", invalidPostReportId, duration));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("banUserDueToPost"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorCode.POST_REPORT_NOT_FOUND.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.POST_REPORT_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("포스트로 인한 유저 제재 - 비정상 BanDuration")
	void 비정상_포스트로_인한_유저_제재_비정상_BanDuration() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";
		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);
		long postId = 1L;

		PostReportDto postReportDto = reportService.createPostReport(request, "test1@example.com", postId);
		String invalidDuration = "비정상";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/ban?duration={duration}", postReportDto.id(), invalidDuration));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("banUserDueToPost"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_BAN_DURATION.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.INVALID_BAN_DURATION.getMessage()));
	}

	@Test
	@DisplayName("포스트로 인한 유저 제재 - 비로그인")
	void 비정상_포스트로_인한_유저_제재_비로그인() throws Exception {

		long temp = 1L;
		String duration = "7일";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/ban?duration={duration}", temp, duration));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("포스트로 인한 유저 제재 - 관리자 권한 없음")
	void 비정상_포스트로_인한_유저_제재_관리자_권한_없음() throws Exception {
		setUpUserAuth(1L);

		long temp = 1L;
		String duration = "7일";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/ban?duration={duration}", temp, duration));

		resultActions
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
	}

	@Test
	@DisplayName("댓글로 인한 유저 제재 - 정상")
	void 정상_댓글로_인한_유저_제재() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";
		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);
		long postCommentId = 1L;

		PostCommentReportDto postCommentReportDto = reportService.createPostCommentReport(request, "test1@example.com", postCommentId);
		String duration = "7일";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/ban?duration={duration}", postCommentReportDto.id(), duration));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("banUserDueToPostComment"))
			.andExpect(status().isOk());

		assertThat(postCommentService.findPostById(postCommentId).getUser().isBanned()).isTrue();
	}

	@Test
	@DisplayName("댓글로 인한 유저 제재 - 존재하지 않는 신고")
	void 비정상_댓글로_인한_유저_제재_신고_존재_X() throws Exception {
		setUpAdminAuth("admin");

		long invalidPostCommentReportId = -1L;
		String duration = "7일";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/ban?duration={duration}", invalidPostCommentReportId, duration));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("banUserDueToPostComment"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_REPORT_NOT_FOUND.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.COMMENT_REPORT_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("댓글로 인한 유저 제재 - 비정상 BanDuration")
	void 비정상_댓글로_인한_유저_제재_비정상_BanDuration() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";
		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);
		long postId = 1L;

		PostCommentReportDto postcommentReportDto = reportService.createPostCommentReport(request, "test1@example.com", postId);
		String invalidDuration = "비정상";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/ban?duration={duration}", postcommentReportDto.id(), invalidDuration));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("banUserDueToPostComment"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_BAN_DURATION.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.INVALID_BAN_DURATION.getMessage()));
	}

	@Test
	@DisplayName("댓글로 인한 유저 제재 - 비로그인")
	void 비정상_댓글로_인한_유저_제재_비로그인() throws Exception {

		long temp = 1L;
		String duration = "7일";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/ban?duration={duration}", temp, duration));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("댓글로 인한 유저 제재 - 관리자 권한 없음")
	void 비정상_댓글로_인한_유저_제재_관리자_권한_없음() throws Exception {
		setUpUserAuth(1L);

		long temp = 1L;
		String duration = "7일";

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/ban?duration={duration}", temp, duration));

		resultActions
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
	}


	@Test
	@DisplayName("포스트 신고 기각 - 정상")
	void 정상_포스트_신고_기각() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";
		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);
		long postId = 1L;

		PostReportDto postReportDto = reportService.createPostReport(request, "test1@example.com", postId);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/reject", postReportDto.id()));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("rejectPostReport"))
			.andExpect(status().isOk());

		PostReport postReport = postReportRepository.findById(postReportDto.id())
			.orElseThrow(ErrorCode.POST_REPORT_NOT_FOUND::throwServiceException);

		assertThat(postReport.getReportStatus()).isEqualTo(ReportStatus.REJECTED);
	}

	@Test
	@DisplayName("포스트 신고 기각 - 존재하지 않는 신고")
	void 비정상_포스트_신고_기각_신고_존재_X() throws Exception {
		setUpAdminAuth("admin");

		long invalidPostReportId = -1L;

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/reject", invalidPostReportId));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("rejectPostReport"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorCode.POST_REPORT_NOT_FOUND.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.POST_REPORT_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("포스트 신고 기각 - 비로그인")
	void 비정상_포스트_신고_기각_비로그인() throws Exception {

		long temp = 1L;

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/reject", temp));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("포스트 신고 기각 - 관리자 권한 없음")
	void 비정상_포스트_신고_기각_관리자_권한_없음() throws Exception {
		setUpUserAuth(1L);

		long temp = 1L;

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/post/reject", temp));

		resultActions
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
	}

	@Test
	@DisplayName("댓글 신고 기각 - 정상")
	void 정상_댓글_신고_기각() throws Exception {
		setUpAdminAuth("admin");

		ReportType reportType = ReportType.SPAM;
		String reason = "스팸 신고";
		ReportCreateRequest request = new ReportCreateRequest(reportType, reason);
		long postCommentId = 1L;

		PostCommentReportDto postCommentReportDto = reportService.createPostCommentReport(request, "test1@example.com", postCommentId);

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/reject", postCommentReportDto.id()));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("rejectPostCommentReport"))
			.andExpect(status().isOk());

		PostCommentReport postCommentReport = postCommentReportRepository.findById(postCommentReportDto.id())
			.orElseThrow(ErrorCode.COMMENT_REPORT_NOT_FOUND::throwServiceException);

		assertThat(postCommentReport.getReportStatus()).isEqualTo(ReportStatus.REJECTED);
	}

	@Test
	@DisplayName("댓글 신고 기각 - 존재하지 않는 신고")
	void 비정상_댓글_신고_기각_신고_존재_X() throws Exception {
		setUpAdminAuth("admin");

		long invalidPostCommentReportId = -1L;

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/reject", invalidPostCommentReportId));

		resultActions
			.andExpect(handler().handlerType(ReportController.class))
			.andExpect(handler().methodName("rejectPostCommentReport"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_REPORT_NOT_FOUND.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.COMMENT_REPORT_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("댓글 신고 기각 - 비로그인")
	void 비정상_댓글_신고_기각_비로그인() throws Exception {

		long temp = 1L;

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/reject", temp));

		resultActions
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("댓글 신고 기각 - 관리자 권한 없음")
	void 비정상_댓글_신고_기각_관리자_권한_없음() throws Exception {
		setUpUserAuth(1L);

		long temp = 1L;

		ResultActions resultActions = mvc.perform(
			post("/api/v1/reports/{reportId}/comment/reject", temp));

		resultActions
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(ErrorCode.ACCESS_DENIED.getMessage()));
	}
}
