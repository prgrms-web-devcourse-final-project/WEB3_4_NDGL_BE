package com.ndgl.spotfinder.domain.report.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.service.PostCommentService;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.domain.report.dto.PostCommentReportResponse;
import com.ndgl.spotfinder.domain.report.dto.PostReportResponse;
import com.ndgl.spotfinder.domain.report.dto.ReportCreateRequest;
import com.ndgl.spotfinder.domain.report.entity.Ban;
import com.ndgl.spotfinder.domain.report.entity.BanDuration;
import com.ndgl.spotfinder.domain.report.entity.PostCommentReport;
import com.ndgl.spotfinder.domain.report.entity.PostReport;
import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;
import com.ndgl.spotfinder.domain.report.repository.BanRepository;
import com.ndgl.spotfinder.domain.report.repository.PostCommentReportRepository;
import com.ndgl.spotfinder.domain.report.repository.PostReportRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.exception.ServiceException;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
	@Mock
	private PostReportRepository postReportRepository;

	@Mock
	private PostCommentReportRepository postCommentReportRepository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private BanRepository banRepository;

	@Mock
	private UserService userService;

	@Mock
	private PostService postService;

	@Mock
	private PostCommentService postCommentService;

	@InjectMocks
	private ReportService reportService;


	@Test
	@DisplayName("포스트 신고 - 정상")
	void createPostReport_success() {
		// given
		ReportCreateRequest request = new ReportCreateRequest(ReportType.SPAM, "SPAM");
		String reporterEmail = "exmaple1@example.com";
		long postId = 3L;

		User reporter = mock(User.class);
		when(reporter.getId()).thenReturn(1L);

		User reportedUser = mock(User.class);
		when(reportedUser.getId()).thenReturn(1L);

		Post post = mock(Post.class);
		when(post.getId()).thenReturn(1L);

		PostReport savedReport = mock(PostReport.class);
		when(savedReport.getId()).thenReturn(1L);
		when(savedReport.getReporter()).thenReturn(reporter);
		when(savedReport.getReportedUser()).thenReturn(reportedUser);
		when(savedReport.getPost()).thenReturn(post);
		when(savedReport.getReason()).thenReturn("SPAM");
		when(savedReport.getReportType()).thenReturn(ReportType.SPAM);
		when(savedReport.getReportStatus()).thenReturn(ReportStatus.PENDING);

		when(userService.findUserByEmail(reporterEmail)).thenReturn(reporter);
		when(postService.findPostById(postId)).thenReturn(post);
		when(postReportRepository.save(any(PostReport.class))).thenReturn(savedReport);

		ArgumentCaptor<PostReport> reportCaptor = ArgumentCaptor.forClass(PostReport.class);

		// when
		reportService.createPostReport(request, reporterEmail, postId);

		// then
		verify(postReportRepository).save(reportCaptor.capture());

		PostReport capturedReport = reportCaptor.getValue();
		assertThat(capturedReport.getReporter()).isEqualTo(reporter);
		assertThat(capturedReport.getReason()).isEqualTo("SPAM");
		assertThat(capturedReport.getReportType()).isEqualTo(ReportType.SPAM);
		assertThat(capturedReport.getPost()).isEqualTo(post);
	}

	@Test
	@DisplayName("포스트 신고 - 비정상 reporterEmail")
	void createPostReportSlice_invalidReporterId() {
		// given
		String invalidReporterEmail = "exmaple1@example.com";
		long postId = 3L;
		ReportCreateRequest request = new ReportCreateRequest(ReportType.SPAM, "SPAM");

		when(userService.findUserByEmail(invalidReporterEmail))
			.thenThrow(new ServiceException(ErrorCode.REPORTER_NOT_FOUND.getHttpStatus(), ErrorCode.REPORTER_NOT_FOUND.getMessage()));

		// When & Then
		assertThatThrownBy(() -> reportService.createPostReport(request, invalidReporterEmail, postId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REPORTER_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("포스트 신고 - 비정상 postId")
	void createPostReportSlice_invalidPostId() {
		// given
		String reporterEmail = "exmaple1@example.com";
		long invalidPostId = -1L;
		ReportCreateRequest request = new ReportCreateRequest(ReportType.SPAM, "SPAM");

		User reporter = User.builder().build();
		when(userService.findUserByEmail(reporterEmail)).thenReturn(reporter);
		when(postService.findPostById(invalidPostId))
			.thenThrow(new ServiceException(ErrorCode.REPORTED_POST_NOT_FOUND.getHttpStatus(), ErrorCode.REPORTED_POST_NOT_FOUND.getMessage()));

		// When & Then
		assertThatThrownBy(() -> reportService.createPostReport(request, reporterEmail, invalidPostId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REPORTED_POST_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("댓글 신고 - 정상")
	void createPostCommentReport_success() {
		// given
		ReportCreateRequest request = new ReportCreateRequest(ReportType.ADVERTISING, "광고 댓글입니다");
		String reporterEmail = "exmaple1@example.com";
		long postCommentId = 3L;

		User reporter = mock(User.class);
		when(reporter.getId()).thenReturn(1L);

		User reportedUser = mock(User.class);
		when(reportedUser.getId()).thenReturn(1L);

		PostComment postComment = mock(PostComment.class);
		when(postComment.getId()).thenReturn(1L);

		PostCommentReport savedReport = mock(PostCommentReport.class);
		when(savedReport.getId()).thenReturn(1L);
		when(savedReport.getReporter()).thenReturn(reporter);
		when(savedReport.getReportedUser()).thenReturn(reportedUser);
		when(savedReport.getPostComment()).thenReturn(postComment);
		when(savedReport.getReason()).thenReturn("SPAM");
		when(savedReport.getReportType()).thenReturn(ReportType.SPAM);
		when(savedReport.getReportStatus()).thenReturn(ReportStatus.PENDING);

		when(userService.findUserByEmail(reporterEmail)).thenReturn(reporter);
		when(postCommentService.findCommentById(postCommentId)).thenReturn(postComment);
		when(postCommentReportRepository.save(any(PostCommentReport.class))).thenReturn(savedReport);

		ArgumentCaptor<PostCommentReport> reportCaptor = ArgumentCaptor.forClass(PostCommentReport.class);

		// when
		reportService.createPostCommentReport(request, reporterEmail, postCommentId);

		// then
		verify(postCommentReportRepository).save(reportCaptor.capture());

		PostCommentReport capturedReport = reportCaptor.getValue();
		assertThat(capturedReport.getReporter()).isEqualTo(reporter);
		assertThat(capturedReport.getReason()).isEqualTo("광고 댓글입니다");
		assertThat(capturedReport.getReportType()).isEqualTo(ReportType.ADVERTISING);
		assertThat(capturedReport.getPostComment()).isEqualTo(postComment);
	}

	@Test
	@DisplayName("댓글 신고 - 비정상 reporterEmail")
	void createPostCommentReportSlice_invalidReporterId() {
		// given
		String invalidReporterEmail = "exmaple1@example.com";
		long postCommentId = 3L;
		ReportCreateRequest request = new ReportCreateRequest(ReportType.SPAM, "SPAM");

		when(userService.findUserByEmail(invalidReporterEmail))
			.thenThrow(new ServiceException(ErrorCode.REPORTER_NOT_FOUND.getHttpStatus(), ErrorCode.REPORTER_NOT_FOUND.getMessage()));

		// When & Then
		assertThatThrownBy(() -> reportService.createPostCommentReport(request, invalidReporterEmail, postCommentId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REPORTER_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("댓글 신고 - 비정상 postCommentId")
	void createPostCommentReportSlice_invalidPostCommentId() {
		// given
		String reporterEmail = "exmaple1@example.com";
		long invalidPostCommentId = -1L;
		ReportCreateRequest request = new ReportCreateRequest(ReportType.SPAM, "SPAM");

		User reporter = User.builder().build();
		when(userService.findUserByEmail(reporterEmail)).thenReturn(reporter);
		when(postCommentService.findCommentById(invalidPostCommentId))
			.thenThrow(new ServiceException(ErrorCode.REPORTED_COMMENT_NOT_FOUND.getHttpStatus(), ErrorCode.REPORTED_COMMENT_NOT_FOUND.getMessage()));

		// When & Then
		assertThatThrownBy(() -> reportService.createPostCommentReport(request, reporterEmail, invalidPostCommentId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.REPORTED_COMMENT_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 정상")
	void getPostReportSlice_success() {
		//Given
		long lastId = 0L;
		int size = 10;
		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

		PostReportResponse mockReport1 = mock(PostReportResponse.class);
		PostReportResponse mockReport2 = mock(PostReportResponse.class);
		List<PostReportResponse> mockReports = List.of(mockReport1, mockReport2);
		Slice<PostReportResponse> mockSlice = new SliceImpl<>(mockReports, pageable, false);

		when(postReportRepository.findPostReports(lastId, pageable)).thenReturn(mockSlice);

		// when
		SliceResponse<PostReportResponse> result = reportService.getPostReportSlice(lastId, size);

		// then
		verify(postReportRepository).findPostReports(lastId, pageable);

		assertThat(result.contents()).hasSize(2);
		assertThat(result.contents()).containsExactly(mockReport1, mockReport2);
		assertThat(result.hasNext()).isFalse();
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 조회 결과 X")
	void getPostReportSlice_whenNoReportsFound() {
		// given
		long lastId = 0L;
		int size = 10;

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostReportResponse> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

		when(postReportRepository.findPostReports(lastId, pageable)).thenReturn(emptySlice);

		// when, then
		assertThatThrownBy(() -> reportService.getPostReportSlice(lastId, size))
			.isInstanceOf(ServiceException.class)
			.hasMessageContaining(ErrorCode.EMPTY_POST_REPORT_SLICE.getMessage());
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 정상")
	void getPostCommentReportSlice_success() {
		//Given
		long lastId = 0L;
		int size = 10;
		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

		PostCommentReportResponse mockReport1 = mock(PostCommentReportResponse.class);
		PostCommentReportResponse mockReport2 = mock(PostCommentReportResponse.class);
		List<PostCommentReportResponse> mockReports = List.of(mockReport1, mockReport2);
		Slice<PostCommentReportResponse> mockSlice = new SliceImpl<>(mockReports, pageable, false);

		when(postCommentReportRepository.findPostCommentReports(lastId, pageable)).thenReturn(mockSlice);

		// when
		SliceResponse<PostCommentReportResponse> result = reportService.getPostCommentReportSlice(lastId, size);

		// then
		verify(postCommentReportRepository).findPostCommentReports(lastId, pageable);

		assertThat(result.contents()).hasSize(2);
		assertThat(result.contents()).containsExactly(mockReport1, mockReport2);
		assertThat(result.hasNext()).isFalse();
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 조회 결과 X")
	void getPostCommentReportSlice_whenNoReportsFound() {
		// given
		long lastId = 0L;
		int size = 10;

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostCommentReportResponse> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

		when(postCommentReportRepository.findPostCommentReports(lastId, pageable)).thenReturn(emptySlice);

		// when, then
		assertThatThrownBy(() -> reportService.getPostCommentReportSlice(lastId, size))
			.isInstanceOf(ServiceException.class)
			.hasMessageContaining(ErrorCode.EMPTY_COMMENT_REPORT_SLICE.getMessage());
	}

	@Test
	@DisplayName("포스트 유저 제재 - 정상")
	void banUserDueToPost_success() {
		// Given
		long reportId = 1L;
		String duration = "30일";

		User user = User.builder().id(1L).build();
		PostReport report = PostReport.builder().reportedUser(user).build();

		Ban ban = mock(Ban.class);
		when(ban.getId()).thenReturn(1L);
		when(ban.getUser()).thenReturn(user);

		when(postReportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(banRepository.save(any(Ban.class))).thenReturn(ban);

		ArgumentCaptor<Ban> banCaptor = ArgumentCaptor.forClass(Ban.class);

		// When
		reportService.banUserDueToPost(reportId, duration);

		// Then
		verify(banRepository).save(banCaptor.capture());

		Ban capturedBan = banCaptor.getValue();
		assertThat(capturedBan.getUser()).isEqualTo(user);
		assertThat(capturedBan.getStartDate()).isEqualTo(LocalDate.now());
		assertThat(capturedBan.getEndDate()).isEqualTo(Ban.calculateEndDate(BanDuration.ONE_MONTH));
		assertThat(report.getReportType()).isEqualTo(capturedBan.getBanType());
		assertThat(user.isBanned()).isTrue();
		assertThat(report.getReportStatus()).isEqualTo(ReportStatus.RESOLVED);
	}

	@Test
	@DisplayName("포스트 유저 제재 - 비정상 duration")
	void banUserDueToPost_invalidDuration() {
		// Given
		long reportId = -1L;
		String duration = "에러";

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPost(reportId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.INVALID_BAN_DURATION.getMessage());
			});
	}

	@Test
	@DisplayName("포스트 유저 제재 - 비정상 reportId")
	void banUserDueToPost_invalidReportId() {
		// Given
		long invalidReportId = -1L;
		String duration = "30일";

		when(postReportRepository.findById(invalidReportId))
			.thenThrow(new ServiceException(ErrorCode.POST_REPORT_NOT_FOUND.getHttpStatus(), ErrorCode.POST_REPORT_NOT_FOUND.getMessage()));

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPost(invalidReportId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.POST_REPORT_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("댓글 유저 제재 - 정상")
	void banUserDueToPostComment_success() {
		// Given
		long reportId = 1L;
		String duration = "7일";

		User user = User.builder().id(1L).build();

		PostCommentReport report = PostCommentReport.builder().reportedUser(user).build();

		Ban ban = mock(Ban.class);
		when(ban.getId()).thenReturn(1L);
		when(ban.getUser()).thenReturn(user);

		when(postCommentReportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(banRepository.save(any(Ban.class))).thenReturn(ban);

		ArgumentCaptor<Ban> banCaptor = ArgumentCaptor.forClass(Ban.class);

		// When
		reportService.banUserDueToPostComment(reportId, duration);

		// Then
		verify(banRepository).save(banCaptor.capture());

		Ban capturedBan = banCaptor.getValue();
		assertThat(capturedBan.getUser()).isEqualTo(user);
		assertThat(capturedBan.getStartDate()).isEqualTo(LocalDate.now());
		assertThat(capturedBan.getEndDate()).isEqualTo(Ban.calculateEndDate(BanDuration.ONE_WEEK));
		assertThat(capturedBan.getBanType()).isEqualTo(report.getReportType());
		assertThat(user.isBanned()).isTrue();
		assertThat(report.getReportStatus()).isEqualTo(ReportStatus.RESOLVED);
	}

	@Test
	@DisplayName("댓글 유저 제재 - 비정상 duration")
	void banUserDueToPostComment_invalidDuration() {
		// Given
		long reportId = -1L;
		String duration = "에러";

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPostComment(reportId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.INVALID_BAN_DURATION.getMessage());
			});
	}

	@Test
	@DisplayName("댓글 유저 제재 - 비정상 reportId")
	void banUserDueToPostComment_invalidReportId() {
		// Given
		long invalidReportId = -1L;
		String duration = "30일";

		when(postCommentReportRepository.findById(invalidReportId))
			.thenThrow(new ServiceException(ErrorCode.COMMENT_REPORT_NOT_FOUND.getHttpStatus(), ErrorCode.COMMENT_REPORT_NOT_FOUND.getMessage()));

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPostComment(invalidReportId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.COMMENT_REPORT_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("포스트 신고 기각 - 정상")
	void rejectPostReport_success() {
		// Given
		long reportId = 1L;

		PostReport postReport = PostReport.builder().build();
		when(postReportRepository.findById(reportId)).thenReturn(Optional.of(postReport));

		// When
		reportService.rejectPostReport(reportId);

		// Then
		assertThat(postReport.getReportStatus()).isEqualTo(ReportStatus.REJECTED);
	}

	@Test
	@DisplayName("포스트 신고 기각 - 비정상 reportId")
	void rejectPostReport_invalidReportId() {
		// Given
		long reportId = -1L;

		when(postReportRepository.findById(reportId))
			.thenThrow(new ServiceException(ErrorCode.POST_REPORT_NOT_FOUND.getHttpStatus(), ErrorCode.POST_REPORT_NOT_FOUND.getMessage()));

		// When, Then
		assertThatThrownBy(() -> reportService.rejectPostReport(reportId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.POST_REPORT_NOT_FOUND.getMessage());
			});
	}

	@Test
	@DisplayName("댓글 신고 기각 - 정상")
	void rejectPostCommentReport_success() {
		// Given
		long reportId = 1L;

		PostCommentReport postCommentReport = PostCommentReport.builder().build();
		when(postCommentReportRepository.findById(reportId)).thenReturn(Optional.of(postCommentReport));

		// When
		reportService.rejectPostCommentReport(reportId);

		// Then
		assertThat(postCommentReport.getReportStatus()).isEqualTo(ReportStatus.REJECTED);
	}

	@Test
	@DisplayName("댓글 신고 기각 - 비정상 reportId")
	void rejectPostCommentReport_invalidReportId() {
		// Given
		long reportId = -1L;

		when(postCommentReportRepository.findById(reportId))
			.thenThrow(new ServiceException(ErrorCode.COMMENT_REPORT_NOT_FOUND.getHttpStatus(), ErrorCode.COMMENT_REPORT_NOT_FOUND.getMessage()));

		// When, Then
		assertThatThrownBy(() -> reportService.rejectPostCommentReport(reportId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo(ErrorCode.COMMENT_REPORT_NOT_FOUND.getMessage());
			});
	}
}
