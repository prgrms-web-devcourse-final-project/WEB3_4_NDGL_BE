package com.ndgl.spotfinder.domain.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.report.dto.CommentReportResponse;
import com.ndgl.spotfinder.domain.report.dto.PostReportResponse;
import com.ndgl.spotfinder.domain.report.dto.ReportCreateRequest;
import com.ndgl.spotfinder.domain.report.service.ReportService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "ReportController")
public class ReportController {
	private final ReportService reportService;

	@PostMapping("/posts/{id}")
	@Operation(summary = "포스트 신고 요청")
	public RsData<Void> reportPost(
		@PathVariable Long id,
		@RequestBody @Valid ReportCreateRequest reportCreateRequest) {
		// SpringSecurity Context 에서 User 획득
		long reporterId = -1;

		reportService.createPostReport(reportCreateRequest, reporterId, id);

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/comments/{id}")
	@Operation(summary = "댓글 신고 요청")
	public RsData<Void> reportComment(
		@PathVariable Long id,
		@RequestBody @Valid ReportCreateRequest reportCreateRequest) {
		// SpringSecurity Context 에서 User 획득
		long reporterId = 1;

		reportService.createCommentReport(reportCreateRequest, reporterId, id);

		return RsData.success(HttpStatus.OK);
	}

	@GetMapping("/comments/posts")
	@Operation(summary = "포스트 신고 목록 조회")
	public RsData<SliceResponse<PostReportResponse>> getPostReportList(
		@RequestParam @Valid SliceRequest sliceRequest
	){
		// Admin 계정 체크

		SliceResponse<PostReportResponse> postReportSlice
			= reportService.getPostReportSlice(sliceRequest.lastId(), sliceRequest.size());

		return RsData.success(HttpStatus.OK, postReportSlice);
	}

	@GetMapping("/comments/posts")
	@Operation(summary = "댓글 신고 목록 조회")
	public RsData<SliceResponse<CommentReportResponse>> getCommentReportList(
		@RequestParam @Valid SliceRequest sliceRequest
	){
		// Admin 계정 체크

		SliceResponse<CommentReportResponse> commentReportSlice
			= reportService.getCommentReportSlice(sliceRequest.lastId(), sliceRequest.size());

		return RsData.success(HttpStatus.OK, commentReportSlice);
	}

}
