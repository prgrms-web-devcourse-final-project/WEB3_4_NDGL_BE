package com.ndgl.spotfinder.domain.report.service;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
import com.ndgl.spotfinder.domain.report.test.Post;
import com.ndgl.spotfinder.domain.report.test.PostComment;
import com.ndgl.spotfinder.domain.report.test.User;
import com.ndgl.spotfinder.domain.report.test.UserRepository;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ServiceException;

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
	private final UserRepository userRepository;
	private final BanRepository banRepository;

	// 포스트 신고 생성
	public void createPostReport(
		ReportCreateRequest reportCreateRequest,
		Long reporterId,
		Long postId) {

		if(reporterId < 0) {
			throw new ServiceException(HttpStatus.NOT_FOUND, "예외 발생");
		}

		User reporter = entityManager.getReference(User.class, reporterId);
		User reportedUser = entityManager.getReference(User.class, reportCreateRequest.reportedUserId());
		Post post = entityManager.getReference(Post.class, postId);

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
		Long reporterId,
		Long commentId) {

		if(reporterId < 0) {
			throw new ServiceException(HttpStatus.NOT_FOUND, "예외 발생");
		}

		User reporter = entityManager.getReference(User.class, reporterId);
		User reportedUser = entityManager.getReference(User.class, reportCreateRequest.reportedUserId());
		PostComment postComment = entityManager.getReference(PostComment.class, commentId);

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
		if(lastId < 0) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "lastId 값은 음수일 수 없습니다.");
		}

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostReportResponse> postReportSlice = postReportRepository.findPostReports(pageable);

		if(postReportSlice.isEmpty()) {
			throw new ServiceException(HttpStatus.NOT_FOUND, "포스트 신고 데이터를 찾지 못했습니다.");
		}

		return new SliceResponse<>(postReportSlice.getContent(), postReportSlice.hasNext());
	}

	// 댓글 신고 목록 조회
	public SliceResponse<PostCommentReportResponse> getCommentReportSlice(long lastId, int size) {
		if(lastId < 0) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "lastId 값은 음수일 수 없습니다.");
		}

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostCommentReportResponse> postCommentReportSlice = postCommentReportRepository.findCommentReports(pageable);

		if(postCommentReportSlice.isEmpty()) {
			throw new ServiceException(HttpStatus.NOT_FOUND, "댓글 신고 데이터를 찾지 못했습니다.");
		}

		return new SliceResponse<>(postCommentReportSlice.getContent(), postCommentReportSlice.hasNext());
	}

	// 포스트로 인한 유저 제재
	public void banUserDueToPost(long reportId, long userId, String duration) {
		// 제재 기간 검증
		BanDuration banDuration = BanDuration.fromString(duration);

		User user = userRepository.findById(userId).orElseThrow(
			() -> new ServiceException(HttpStatus.NOT_FOUND, "제재할 대상이 존재하지 않습니다."));
		user.setBanned(true);

		PostReport postReport = postReportRepository.findById(reportId).orElseThrow(
			() -> new ServiceException(HttpStatus.NOT_FOUND, "존재하지 않는 신고입니다."));
		postReport.setReportStatus(ReportStatus.RESOLVED);

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

		User user = userRepository.findById(userId).orElseThrow(
			() -> new ServiceException(HttpStatus.NOT_FOUND, "제재할 대상이 존재하지 않습니다."));
		user.setBanned(true);

		PostCommentReport postCommentReport = postCommentReportRepository.findById(reportId).orElseThrow(
			() -> new ServiceException(HttpStatus.NOT_FOUND, "존재하지 않는 신고입니다."));

		postCommentReport.setReportStatus(ReportStatus.RESOLVED);

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
		PostReport postReport = postReportRepository.findById(reportId).orElseThrow(
			() -> new ServiceException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글 신고입니다."));
		postReport.setReportStatus(ReportStatus.REJECTED);
	}

	// 댓글 신고 기각
	public void rejectPostCommentReport(long reportId) {
		PostCommentReport postCommentReport = postCommentReportRepository.findById(reportId).orElseThrow(
			() -> new ServiceException(HttpStatus.NOT_FOUND, "존재하지 않는 포스트 신고입니다."));

		postCommentReport.setReportStatus(ReportStatus.REJECTED);
	}
}
