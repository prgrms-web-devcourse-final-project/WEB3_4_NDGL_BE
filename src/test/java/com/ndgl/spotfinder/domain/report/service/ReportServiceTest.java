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
import com.ndgl.spotfinder.domain.report.test.Post;
import com.ndgl.spotfinder.domain.report.test.PostComment;
import com.ndgl.spotfinder.domain.report.test.PostCommentRepository;
import com.ndgl.spotfinder.domain.report.test.PostRepository;
import com.ndgl.spotfinder.domain.report.test.User;
import com.ndgl.spotfinder.domain.report.test.UserRepository;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
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
	private UserRepository userRepository;

	// TODO: 이후 Service 로 의존성 교체 필요
	@Mock
	private PostRepository postRepository;

	@Mock
	private PostCommentRepository postCommentRepository;

	@InjectMocks
	private ReportService reportService;


	@Test
	@DisplayName("포스트 신고 - 정상")
	void createPostReport_success() {
		// given
		ReportCreateRequest request = new ReportCreateRequest(2L, ReportType.SPAM, "SPAM");
		long reporterId = 1L;
		long postId = 3L;

		User reporter = User.builder().build();
		User reportedUser = User.builder().build();
		Post post = Post.builder().build();

		when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
		when(userRepository.findById(request.reportedUserId())).thenReturn(Optional.of(reportedUser));
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));

		ArgumentCaptor<PostReport> reportCaptor = ArgumentCaptor.forClass(PostReport.class);

		// when
		reportService.createPostReport(request, reporterId, postId);

		// then
		verify(postReportRepository).save(reportCaptor.capture());

		PostReport capturedReport = reportCaptor.getValue();
		assertThat(capturedReport.getReporter()).isEqualTo(reporter);
		assertThat(capturedReport.getReportedUser()).isEqualTo(reportedUser);
		assertThat(capturedReport.getReason()).isEqualTo("SPAM");
		assertThat(capturedReport.getReportType()).isEqualTo(ReportType.SPAM);
		assertThat(capturedReport.getPost()).isEqualTo(post);
	}

	@Test
	@DisplayName("포스트 신고 - 비정상 reporterId")
	void createPostReportSlice_invalidReporterId() {
		// given
		long invalidReporterId = -1L;
		long reportedUserId = 2L;
		long postId = 3L;
		ReportCreateRequest request = new ReportCreateRequest(reportedUserId, ReportType.SPAM, "SPAM");

		when(userRepository.findById(invalidReporterId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> reportService.createPostReport(request, invalidReporterId, postId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("reporterId에 해당하는 사용자가 없습니다.");
			});
	}

	@Test
	@DisplayName("포스트 신고 - 비정상 reportedUserId")
	void createPostReportSlice_invalidReportedUserId() {
		// given
		long reporterId = 1L;
		long invalidReportedUserId = -1L;
		long postId = 3L;
		ReportCreateRequest request = new ReportCreateRequest(invalidReportedUserId, ReportType.SPAM, "SPAM");

		User user = User.builder().build();
		when(userRepository.findById(reporterId)).thenReturn(Optional.of(user));
		when(userRepository.findById(invalidReportedUserId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> reportService.createPostReport(request, reporterId, postId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("reportedUserId에 해당하는 사용자가 없습니다.");
			});
	}

	@Test
	@DisplayName("포스트 신고 - 비정상 postId")
	void createPostReportSlice_invalidPostId() {
		// given
		long reporterId = 1L;
		long reportedUserId = 2L;
		long invalidPostId = -1L;
		ReportCreateRequest request = new ReportCreateRequest(reportedUserId, ReportType.SPAM, "SPAM");

		User reporter = User.builder().build();
		User reportedUser = User.builder().build();
		when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
		when(userRepository.findById(reportedUserId)).thenReturn(Optional.of(reportedUser));
		when(postRepository.findById(invalidPostId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> reportService.createPostReport(request, reporterId, invalidPostId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("postId에 해당하는 포스트가 없습니다.");
			});
	}

	@Test
	@DisplayName("댓글 신고 - 정상")
	void createPostCommentReport_success() {
		// given
		ReportCreateRequest request = new ReportCreateRequest(2L, ReportType.ADVERTISING, "광고 댓글입니다");
		long reporterId = 1L;
		long postCommentId = 3L;

		User reporter = User.builder().build();
		User reportedUser = User.builder().build();
		PostComment postComment = PostComment.builder().build();

		when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
		when(userRepository.findById(request.reportedUserId())).thenReturn(Optional.of(reportedUser));
		when(postCommentRepository.findById(postCommentId)).thenReturn(Optional.of(postComment));

		ArgumentCaptor<PostCommentReport> reportCaptor = ArgumentCaptor.forClass(PostCommentReport.class);

		// when
		reportService.createPostCommentReport(request, reporterId, postCommentId);

		// then
		verify(postCommentReportRepository).save(reportCaptor.capture());

		PostCommentReport capturedReport = reportCaptor.getValue();
		assertThat(capturedReport.getReporter()).isEqualTo(reporter);
		assertThat(capturedReport.getReportedUser()).isEqualTo(reportedUser);
		assertThat(capturedReport.getReason()).isEqualTo("광고 댓글입니다");
		assertThat(capturedReport.getReportType()).isEqualTo(ReportType.ADVERTISING);
		assertThat(capturedReport.getPostComment()).isEqualTo(postComment);
	}

	@Test
	@DisplayName("댓글 신고 - 비정상 reporterId")
	void createPostCommentReportSlice_invalidReporterId() {
		// given
		long invalidReporterId = -1L;
		long reportedUserId = 2L;
		long postCommentId = 3L;
		ReportCreateRequest request = new ReportCreateRequest(reportedUserId, ReportType.SPAM, "SPAM");

		when(userRepository.findById(invalidReporterId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> reportService.createPostCommentReport(request, invalidReporterId, postCommentId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("reporterId에 해당하는 사용자가 없습니다.");
			});
	}

	@Test
	@DisplayName("포스트 신고 - 비정상 reportedUserId")
	void createPostCommentReportSlice_invalidReportedUserId() {
		// given
		long reporterId = 1L;
		long invalidReportedUserId = -1L;
		long postCommentId = 3L;
		ReportCreateRequest request = new ReportCreateRequest(invalidReportedUserId, ReportType.SPAM, "SPAM");

		User user = User.builder().build();
		when(userRepository.findById(reporterId)).thenReturn(Optional.of(user));
		when(userRepository.findById(invalidReportedUserId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> reportService.createPostCommentReport(request, reporterId, postCommentId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("reportedUserId에 해당하는 사용자가 없습니다.");
			});
	}

	@Test
	@DisplayName("포스트 신고 - 비정상 postId")
	void createPostCommentReportSlice_invalidPostId() {
		// given
		long reporterId = 1L;
		long reportedUserId = 2L;
		long invalidPostCommentId = -1L;
		ReportCreateRequest request = new ReportCreateRequest(reportedUserId, ReportType.SPAM, "SPAM");

		User reporter = User.builder().build();
		User reportedUser = User.builder().build();
		when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
		when(userRepository.findById(reportedUserId)).thenReturn(Optional.of(reportedUser));
		when(postCommentRepository.findById(invalidPostCommentId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> reportService.createPostCommentReport(request, reporterId, invalidPostCommentId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("postCommentId에 해당하는 댓글이 없습니다.");
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

		when(postReportRepository.findPostReports(pageable)).thenReturn(mockSlice);

		// when
		SliceResponse<PostReportResponse> result = reportService.getPostReportSlice(lastId, size);

		// then
		verify(postReportRepository).findPostReports(pageable);

		assertThat(result.contents()).hasSize(2);
		assertThat(result.contents()).containsExactly(mockReport1, mockReport2);
		assertThat(result.hasNext()).isFalse();
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 비정상 lastId")
	void getPostReportSlice_invalidLastId() {
		// given
		long negativeLastId = -1L;
		int size = 10;

		// when, then
		assertThatThrownBy(() -> reportService.getPostReportSlice(negativeLastId, size))
			.isInstanceOf(ServiceException.class)
			.hasMessageContaining("lastId 값은 음수일 수 없습니다.");
	}

	@Test
	@DisplayName("포스트 신고 목록 조회 - 조회 결과 X")
	void getPostReportSlice_whenNoReportsFound() {
		// given
		long lastId = 0L;
		int size = 10;

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostReportResponse> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

		when(postReportRepository.findPostReports(pageable)).thenReturn(emptySlice);

		// when, then
		assertThatThrownBy(() -> reportService.getPostReportSlice(lastId, size))
			.isInstanceOf(ServiceException.class)
			.hasMessageContaining("포스트 신고 데이터를 찾지 못했습니다.");
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

		when(postCommentReportRepository.findPostCommentReports(pageable)).thenReturn(mockSlice);

		// when
		SliceResponse<PostCommentReportResponse> result = reportService.getPostCommentReportSlice(lastId, size);

		// then
		verify(postCommentReportRepository).findPostCommentReports(pageable);

		assertThat(result.contents()).hasSize(2);
		assertThat(result.contents()).containsExactly(mockReport1, mockReport2);
		assertThat(result.hasNext()).isFalse();
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 비정상 lastId")
	void getPostCommentReportSlice_invalidLastId() {
		// given
		long negativeLastId = -1L;
		int size = 10;

		// when, then
		assertThatThrownBy(() -> reportService.getPostCommentReportSlice(negativeLastId, size))
			.isInstanceOf(ServiceException.class)
			.hasMessageContaining("lastId 값은 음수일 수 없습니다.");
	}

	@Test
	@DisplayName("댓글 신고 목록 조회 - 조회 결과 X")
	void getPostCommentReportSlice_whenNoReportsFound() {
		// given
		long lastId = 0L;
		int size = 10;

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostCommentReportResponse> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

		when(postCommentReportRepository.findPostCommentReports(pageable)).thenReturn(emptySlice);

		// when, then
		assertThatThrownBy(() -> reportService.getPostCommentReportSlice(lastId, size))
			.isInstanceOf(ServiceException.class)
			.hasMessageContaining("댓글 신고 데이터를 찾지 못했습니다.");
	}

	@Test
	@DisplayName("포스트 유저 제재 - 정상")
	void banUserDueToPost_success() {
		// Given
		long userId = 1L;
		long reportId = 1L;
		String duration = "30일";

		User user = User.builder().build();
		PostReport report = PostReport.builder().build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(postReportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(banRepository.save(any(Ban.class))).thenReturn(Ban.builder().build());

		ArgumentCaptor<Ban> banCaptor = ArgumentCaptor.forClass(Ban.class);

		// When
		reportService.banUserDueToPost(reportId, userId, duration);

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
		long userId = 1L;
		String duration = "에러";

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPost(reportId, userId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(serviceException.getMessage()).isEqualTo("유효하지 않은 제재 일자 옵션입니다.");
			});
	}

	@Test
	@DisplayName("포스트 유저 제재 - 비정상 userId")
	void banUserDueToPost_invalidUserId() {
		// Given
		long reportId = 1L;
		long invalidUserId = -1L;
		String duration = "30일";

		PostReport report = PostReport.builder().build();
		when(postReportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPost(reportId, invalidUserId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("제재할 대상이 존재하지 않습니다.");
			});
	}

	@Test
	@DisplayName("포스트 유저 제재 - 비정상 reportId")
	void banUserDueToPost_invalidReportId() {
		// Given
		long userId = 1L;
		long invalidReportId = -1L;
		String duration = "30일";

		when(postReportRepository.findById(invalidReportId)).thenReturn(Optional.empty());

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPost(invalidReportId, userId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("존재하지 않는 신고입니다.");
			});
	}

	@Test
	@DisplayName("댓글 유저 제재 - 정상")
	void banUserDueToPostComment_success() {
		// Given
		long reportId = 1L;
		long userId = 1L;
		String duration = "7일";

		User user = User.builder().build();
		PostCommentReport report = PostCommentReport.builder().build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(postCommentReportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(banRepository.save(any(Ban.class))).thenReturn(Ban.builder().build());

		ArgumentCaptor<Ban> banCaptor = ArgumentCaptor.forClass(Ban.class);

		// When
		reportService.banUserDueToPostComment(reportId, userId, duration);

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
		long userId = 1L;
		String duration = "에러";

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPostComment(reportId, userId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(serviceException.getMessage()).isEqualTo("유효하지 않은 제재 일자 옵션입니다.");
			});
	}

	@Test
	@DisplayName("댓글 유저 제재 - 비정상 userId")
	void banUserDueToPostComment_invalidUserId() {
		// Given
		long reportId = -1L;
		long invalidUserId = 1L;
		String duration = "30일";

		PostCommentReport report = PostCommentReport.builder().build();

		when(postCommentReportRepository.findById(reportId)).thenReturn(Optional.of(report));
		when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPostComment(reportId, invalidUserId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("제재할 대상이 존재하지 않습니다.");
			});
	}

	@Test
	@DisplayName("댓글 유저 제재 - 비정상 reportId")
	void banUserDueToPostComment_invalidReportId() {
		// Given
		long userId = 1L;
		long invalidReportId = -1L;
		String duration = "30일";

		when(postCommentReportRepository.findById(invalidReportId)).thenReturn(Optional.empty());

		// When
		assertThatThrownBy(() -> reportService.banUserDueToPostComment(invalidReportId, userId, duration))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("존재하지 않는 신고입니다.");
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

		when(postReportRepository.findById(reportId)).thenReturn(Optional.empty());

		// When, Then
		assertThatThrownBy(() -> reportService.rejectPostReport(reportId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("존재하지 않는 포스트 신고입니다.");
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

		when(postCommentReportRepository.findById(reportId)).thenReturn(Optional.empty());

		// When, Then
		assertThatThrownBy(() -> reportService.rejectPostCommentReport(reportId))
			.isInstanceOf(ServiceException.class)
			.satisfies(exception -> {
				ServiceException serviceException = (ServiceException) exception;
				assertThat(serviceException.getCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(serviceException.getMessage()).isEqualTo("존재하지 않는 댓글 신고입니다.");
			});
	}
}
