package com.ndgl.spotfinder.domain.report.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.report.dto.CommentReportResponse;
import com.ndgl.spotfinder.domain.report.dto.PostReportResponse;
import com.ndgl.spotfinder.domain.report.dto.ReportCreateRequest;
import com.ndgl.spotfinder.domain.report.entity.CommentReport;
import com.ndgl.spotfinder.domain.report.entity.PostReport;
import com.ndgl.spotfinder.domain.report.repository.CommentReportRepository;
import com.ndgl.spotfinder.domain.report.repository.PostReportRepository;
import com.ndgl.spotfinder.domain.report.test.Post;
import com.ndgl.spotfinder.domain.report.test.PostComment;
import com.ndgl.spotfinder.domain.report.test.User;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ServiceException;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final PostReportRepository postReportRepository;
	private final CommentReportRepository commentReportRepository;
	private final EntityManager entityManager;

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

	public void createCommentReport(
		ReportCreateRequest reportCreateRequest,
		Long reporterId,
		Long commentId) {

		if(reporterId < 0) {
			throw new ServiceException(HttpStatus.NOT_FOUND, "예외 발생");
		}

		User reporter = entityManager.getReference(User.class, reporterId);
		User reportedUser = entityManager.getReference(User.class, reportCreateRequest.reportedUserId());
		PostComment postComment = entityManager.getReference(PostComment.class, commentId);

		CommentReport report = CommentReport.builder()
			.reporter(reporter)
			.reportedUser(reportedUser)
			.reason(reportCreateRequest.reason())
			.reportType(reportCreateRequest.reportType())
			.comment(postComment)
			.build();

		commentReportRepository.save(report);
	}

	public SliceResponse<PostReportResponse> getPostReportSlice(long lastId, int size) {
		if(lastId < 0) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "lastId 값은 음수일 수 없습니다.");
		}

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<PostReportResponse> responseSlice = postReportRepository.findPostReports(pageable);

		if(responseSlice.isEmpty()) {
			throw new ServiceException(HttpStatus.NOT_FOUND, "포스트 신고 데이터를 찾지 못했습니다.");
		}

		return new SliceResponse<>(responseSlice.getContent(), responseSlice.hasNext());
	}

	public SliceResponse<CommentReportResponse> getCommentReportSlice(long lastId, int size) {
		if(lastId < 0) {
			throw new ServiceException(HttpStatus.BAD_REQUEST, "lastId 값은 음수일 수 없습니다.");
		}

		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
		Slice<CommentReportResponse> slice = commentReportRepository.findCommentReports(pageable);

		if(slice.isEmpty()) {
			throw new ServiceException(HttpStatus.NOT_FOUND, "댓글 신고 데이터를 찾지 못했습니다.");
		}

		return new SliceResponse<>(slice.getContent(), slice.hasNext());
	}
}
