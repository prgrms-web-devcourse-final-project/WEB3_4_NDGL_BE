package com.ndgl.spotfinder.domain.report.service;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
import com.ndgl.spotfinder.domain.report.repository.BanRepository;
import com.ndgl.spotfinder.domain.report.repository.PostCommentReportRepository;
import com.ndgl.spotfinder.domain.report.repository.PostReportRepository;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

	private final PostReportRepository postReportRepository;
	private final PostCommentReportRepository postCommentReportRepository;
	private final EntityManager entityManager;
	private final BanRepository banRepository;

	private final UserService userService;
	private final PostService postService;
	private final PostCommentService postCommentService;

	// 포스트 신고 생성
	public void createPostReport(
		ReportCreateRequest reportCreateRequest,
		String reporterEmail,
		Long postId) {

		// 레포지토리를 통해 엔티티 조회 및 검증
		User reporter = findReporter(reporterEmail);
		User reportedUser = findReportedUser(reportCreateRequest.reportedUserId());
		Post post = findReportedPost(postId);

		PostReport report = PostReport.builder()
			.reporter(reporter)
			.reportedUser(reportedUser)
			.reason(reportCreateRequest.reason())
			.reportType(reportCreateRequest.reportType())
			.post(post)
			.build();

		postReportRepository.save(report);
	}

	// 댓글 신고 생성
	public void createPostCommentReport(
		ReportCreateRequest reportCreateRequest,
		String reporterEmail,
		Long postCommentId) {

		// 레포지토리를 통해 엔티티 조회 및 검증
		User reporter = findReporter(reporterEmail);
		User reportedUser = findReportedUser(reportCreateRequest.reportedUserId());
		PostComment postComment = findReportedPostComment(postCommentId);

		PostCommentReport report = PostCommentReport.builder()
			.reporter(reporter)
			.reportedUser(reportedUser)
			.reason(reportCreateRequest.reason())
			.reportType(reportCreateRequest.reportType())
			.postComment(postComment)
			.build();

		postCommentReportRepository.save(report);
	}

	// 포스트 신고 목록 조회
	public SliceResponse<PostReportResponse> getPostReportSlice(long lastId, int size) {

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostReportResponse> postReportSlice = postReportRepository.findPostReports(lastId, pageable);

		if(postReportSlice.isEmpty()) {
			ErrorCode.EMPTY_POST_REPORT_SLICE.throwServiceException();
		}

		return new SliceResponse<>(postReportSlice.getContent(), postReportSlice.hasNext());
	}

	// 댓글 신고 목록 조회
	public SliceResponse<PostCommentReportResponse> getPostCommentReportSlice(long lastId, int size) {

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostCommentReportResponse> postCommentReportSlice = postCommentReportRepository.findPostCommentReports(lastId, pageable);

		if(postCommentReportSlice.isEmpty()) {
			ErrorCode.EMPTY_COMMENT_REPORT_SLICE.throwServiceException();
		}

		return new SliceResponse<>(postCommentReportSlice.getContent(), postCommentReportSlice.hasNext());
	}

	// 포스트로 인한 유저 제재
	public void banUserDueToPost(long reportId, long userId, String duration) {
		// 제재 기간 검증
		BanDuration banDuration = BanDuration.fromString(duration);

		PostReport postReport = postReportRepository.findById(reportId)
			.orElseThrow(ErrorCode.POST_REPORT_NOT_FOUND::throwServiceException);

		postReport.setReportStatus(ReportStatus.RESOLVED);

		User user = findUserToBan(userId);
		user.setBanned(true);

		Ban ban = Ban.builder()
			.user(user)
			.startDate(LocalDate.now())
			.endDate(Ban.calculateEndDate(banDuration))
			.banType(postReport.getReportType())
			.build();

		banRepository.save(ban);
	}

	// 댓글로 인한 유저 제재
	public void banUserDueToPostComment(long reportId, long userId, String duration) {
		BanDuration banDuration = BanDuration.fromString(duration);

		PostCommentReport postCommentReport = postCommentReportRepository.findById(reportId)
			.orElseThrow(ErrorCode.COMMENT_REPORT_NOT_FOUND::throwServiceException);

		postCommentReport.setReportStatus(ReportStatus.RESOLVED);

		User user = findUserToBan(userId);
		user.setBanned(true);

		Ban ban = Ban.builder()
			.user(user)
			.startDate(LocalDate.now())
			.endDate(Ban.calculateEndDate(banDuration))
			.banType(postCommentReport.getReportType())
			.build();

		banRepository.save(ban);
	}

	// 포스트 신고 기각
	public void rejectPostReport(long reportId) {
		PostReport postReport = postReportRepository.findById(reportId)
			.orElseThrow(ErrorCode.POST_REPORT_NOT_FOUND::throwServiceException);
		postReport.setReportStatus(ReportStatus.REJECTED);
	}

	// 댓글 신고 기각
	public void rejectPostCommentReport(long reportId) {
		PostCommentReport postCommentReport = postCommentReportRepository.findById(reportId)
			.orElseThrow(ErrorCode.COMMENT_REPORT_NOT_FOUND::throwServiceException);

		postCommentReport.setReportStatus(ReportStatus.REJECTED);
	}

	// 아래의 5개 메서드는 에러를 감싸서 다시 던지는 메서드
	private User findReporter(String email) {
		try {
			return userService.findUserByEmail(email);
		} catch (Exception e) {
			throw ErrorCode.REPORTER_NOT_FOUND.throwServiceException(e);
		}
	}

	private User findReportedUser(Long reportedUserId) {
		try {
			return userService.findUserById(reportedUserId);
		} catch (Exception e) {
			throw ErrorCode.REPORTED_USER_NOT_FOUND.throwServiceException(e);
		}
	}

	private User findUserToBan(Long userId) {
		try {
			return userService.findUserById(userId);
		} catch (Exception e) {
			throw ErrorCode.BAN_USER_NOT_FOUND.throwServiceException(e);
		}
	}

	private Post findReportedPost(Long postId) {
		try {
			return postService.findPostById(postId);
		} catch (Exception e) {
			throw ErrorCode.REPORTED_POST_NOT_FOUND.throwServiceException(e);
		}
	}

	private PostComment findReportedPostComment(Long postCommentId) {
		try {
			return postCommentService.findCommentById(postCommentId);
		} catch (Exception e) {
			throw ErrorCode.REPORTED_COMMENT_NOT_FOUND.throwServiceException(e);
		}
	}

}
