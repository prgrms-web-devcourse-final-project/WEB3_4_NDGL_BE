package com.ndgl.spotfinder.domain.report.controller;

import java.security.Principal;

import com.ndgl.spotfinder.domain.report.dto.BanDto;
import com.ndgl.spotfinder.domain.report.dto.PostCommentReportDto;
import com.ndgl.spotfinder.domain.report.dto.PostCommentReportResponseDto;
import com.ndgl.spotfinder.domain.report.dto.PostReportDto;
import com.ndgl.spotfinder.domain.report.dto.PostReportResponseDto;
import com.ndgl.spotfinder.domain.report.dto.ReportCreateRequestDto;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

public interface ReportApiSpecification {
	@Operation(summary = "게시물 신고 요청")
	RsData<PostReportDto> createPostReport(
		@Parameter(description = "게시물 ID") long id,
		ReportCreateRequestDto reportCreateRequestDto,
		@Parameter(hidden = true) Principal principal
	);

	@Operation(summary = "댓글 신고 요청")
	RsData<PostCommentReportDto> createPostCommentReport(
		@Parameter(description = "댓글 ID") long id,
		ReportCreateRequestDto reportCreateRequestDto,
		@Parameter(hidden = true) Principal principal);

	@Operation(
		summary = "게시물 신고 목록 조회",
		description = "요청한 사이즈만큼 최신순으로 조회"
	)
	RsData<SliceResponse<PostReportResponseDto>> getPostReportList(
		 SliceRequest sliceRequest
	);

	@Operation(summary = "댓글 신고 목록 조회",
		description = "요청한 사이즈만큼 최신순으로 조회"
	)
	RsData<SliceResponse<PostCommentReportResponseDto>> getPostCommentReportList(
		SliceRequest sliceRequest
	);

	@Operation(summary = "게시물로 인한 유저 제재")
	RsData<BanDto> banUserDueToPost(
		@Parameter(description = "게시물 신고 ID") long reportId,
		@Parameter(description = "제재 기간") String duration
	);

	@Operation(summary = "댓글로 인한 유저 제재")
	RsData<BanDto> banUserDueToPostComment(
		@Parameter(description = "댓글 신고 ID") long reportId,
		@Parameter(description = "제재 기간") String duration
	);

	@Operation(summary = "게시물 신고 기각")
	RsData<Void> rejectPostReport(
		@Parameter(description = "게시물 신고 ID") long reportId
	);

	@Operation(summary = "댓글 신고 기각")
	RsData<Void> rejectPostCommentReport(
		@Parameter(description = "댓글 신고 ID") long reportId
	);
}
