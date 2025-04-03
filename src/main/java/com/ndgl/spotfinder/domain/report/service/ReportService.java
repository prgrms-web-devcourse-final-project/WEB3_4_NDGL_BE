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
import com.ndgl.spotfinder.domain.report.dto.BanDto;
import com.ndgl.spotfinder.domain.report.dto.PostCommentReportDto;
import com.ndgl.spotfinder.domain.report.dto.PostCommentReportResponse;
import com.ndgl.spotfinder.domain.report.dto.PostReportDto;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

	private final PostReportRepository postReportRepository;
	private final PostCommentReportRepository postCommentReportRepository;
	private final BanRepository banRepository;

	private final UserService userService;
	private final PostService postService;
	private final PostCommentService postCommentService;

	// 포스트 신고 생성
	public PostReportDto createPostReport(
		ReportCreateRequest reportCreateRequest,
		String reporterEmail,
		Long postId) {

		// 레포지토리를 통해 엔티티 조회 및 검증
		User reporter = findReporter(reporterEmail);
		Post post = findReportedPost(postId);
		User reportedUser = post.getUser();

		PostReport report = PostReport.builder()
			.reporter(reporter)
			.reportedUser(reportedUser)
			.reason(reportCreateRequest.reason())
			.reportType(reportCreateRequest.reportType())
			.post(post)
			.build();

		return new PostReportDto(postReportRepository.save(report));
	}

	// 댓글 신고 생성
	public PostCommentReportDto createPostCommentReport(
		ReportCreateRequest reportCreateRequest,
		String reporterEmail,
		Long postCommentId) {

		// 레포지토리를 통해 엔티티 조회 및 검증
		User reporter = findReporter(reporterEmail);
		PostComment postComment = findReportedPostComment(postCommentId);
		User reportedUser = postComment.getUser();

		PostCommentReport report = PostCommentReport.builder()
			.reporter(reporter)
			.reportedUser(reportedUser)
			.reason(reportCreateRequest.reason())
			.reportType(reportCreateRequest.reportType())
			.postComment(postComment)
			.build();

		return new PostCommentReportDto(postCommentReportRepository.save(report));
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
	public BanDto banUserDueToPost(long reportId, String duration) {
		// 제재 기간 검증
		BanDuration banDuration = BanDuration.fromString(duration);

		PostReport postReport = findPostReport(reportId);
		postReport.setReportStatus(ReportStatus.RESOLVED);

		User user = postReport.getReportedUser();
		user.setBanned(true);

		Ban ban = Ban.builder()
			.user(user)
			.startDate(LocalDate.now())
			.endDate(Ban.calculateEndDate(banDuration))
			.banType(postReport.getReportType())
			.build();

		return new BanDto(banRepository.save(ban));
	}

	// 댓글로 인한 유저 제재
	public BanDto banUserDueToPostComment(long reportId, String duration) {
		BanDuration banDuration = BanDuration.fromString(duration);

		PostCommentReport postCommentReport = findPostCommentReport(reportId);
		postCommentReport.setReportStatus(ReportStatus.RESOLVED);

		User user = postCommentReport.getReportedUser();
		user.setBanned(true);

		Ban ban = Ban.builder()
			.user(user)
			.startDate(LocalDate.now())
			.endDate(Ban.calculateEndDate(banDuration))
			.banType(postCommentReport.getReportType())
			.build();

		return new BanDto(banRepository.save(ban));
	}

	// 포스트 신고 기각
	public void rejectPostReport(long reportId) {
		PostReport postReport = findPostReport(reportId);
		postReport.setReportStatus(ReportStatus.REJECTED);
	}

	// 댓글 신고 기각
	public void rejectPostCommentReport(long reportId) {
		PostCommentReport postCommentReport = findPostCommentReport(reportId);
		postCommentReport.setReportStatus(ReportStatus.REJECTED);
	}

	private PostReport findPostReport(long postReportId) {
		return postReportRepository.findById(postReportId)
			.orElseThrow(ErrorCode.POST_REPORT_NOT_FOUND::throwServiceException);
	}

	private PostCommentReport findPostCommentReport(long postCommentReportId) {
		return postCommentReportRepository.findById(postCommentReportId)
			.orElseThrow(ErrorCode.COMMENT_REPORT_NOT_FOUND::throwServiceException);
	}

	// 아래의 5개 메서드는 에러를 감싸서 다시 던지는 메서드
	private User findReporter(String email) {
		try {
			return userService.findUserByEmail(email);
		} catch (Exception e) {
			throw ErrorCode.REPORTER_NOT_FOUND.throwServiceException(e);
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
