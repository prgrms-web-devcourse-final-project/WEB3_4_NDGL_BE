package com.ndgl.spotfinder.domain.report.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.report.dto.PostCommentReportResponse;
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
	public RsData<Void> createPostReport(
		@PathVariable Long id,
		@RequestBody @Valid ReportCreateRequest reportCreateRequest,
		Principal principal) {

		reportService.createPostReport(reportCreateRequest, principal.getName(), id);

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/comments/{id}")
	@Operation(summary = "댓글 신고 요청")
	public RsData<Void> createPostCommentReport(
		@PathVariable Long id,
		@RequestBody @Valid ReportCreateRequest reportCreateRequest,
		Principal principal) {

		reportService.createPostCommentReport(reportCreateRequest, principal.getName(), id);

		return RsData.success(HttpStatus.OK);
	}

	@GetMapping("/posts")
	@Operation(summary = "포스트 신고 목록 조회")
	public RsData<SliceResponse<PostReportResponse>> getPostReportList(
		@ModelAttribute @Valid SliceRequest sliceRequest
	){
		SliceResponse<PostReportResponse> postReportSlice
			= reportService.getPostReportSlice(sliceRequest.lastId(), sliceRequest.size());

		return RsData.success(HttpStatus.OK, postReportSlice);
	}

	@GetMapping("/comments")
	@Operation(summary = "댓글 신고 목록 조회")
	public RsData<SliceResponse<PostCommentReportResponse>> getCommentReportList(
		@ModelAttribute @Valid SliceRequest sliceRequest
	){
		SliceResponse<PostCommentReportResponse> commentReportSlice
			= reportService.getPostCommentReportSlice(sliceRequest.lastId(), sliceRequest.size());

		return RsData.success(HttpStatus.OK, commentReportSlice);
	}

	@PostMapping("/{reportId}/post/ban/{userId}")
	@Operation(summary = "포스트로 인한 유저 제재")
	public RsData<Void> banUserDueToPost(
		@PathVariable long reportId,
		@PathVariable long userId,
		@RequestParam String duration
	){
		reportService.banUserDueToPost(reportId, userId, duration);

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/{reportId}/comment/ban/{userId}")
	@Operation(summary = "댓글로 인한 유저 제재")
	public RsData<Void> banUserDueToPostComment(
		@PathVariable long reportId,
		@PathVariable long userId,
		@RequestParam String duration
	){
		reportService.banUserDueToPostComment(reportId, userId, duration);

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/{reportId}/post/reject")
	@Operation(summary = "포스트 신고 기각")
	public RsData<Void> rejectPostReport(
		@PathVariable long reportId
	){
		reportService.rejectPostReport(reportId);

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/{reportId}/comment/reject")
	@Operation(summary = "댓글 신고 기각")
	public RsData<Void> rejectPostCommentReport(
		@PathVariable long reportId
	){
		reportService.rejectPostCommentReport(reportId);

		return RsData.success(HttpStatus.OK);
	}
}
