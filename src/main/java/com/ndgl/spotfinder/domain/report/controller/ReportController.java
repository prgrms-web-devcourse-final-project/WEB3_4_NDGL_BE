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

import com.ndgl.spotfinder.domain.report.dto.BanDto;
import com.ndgl.spotfinder.domain.report.dto.PostCommentReportDto;
import com.ndgl.spotfinder.domain.report.dto.PostCommentReportResponseDto;
import com.ndgl.spotfinder.domain.report.dto.PostReportDto;
import com.ndgl.spotfinder.domain.report.dto.PostReportResponseDto;
import com.ndgl.spotfinder.domain.report.dto.ReportCreateRequestDto;
import com.ndgl.spotfinder.domain.report.service.ReportService;
import com.ndgl.spotfinder.global.common.dto.SliceRequest;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "신고")
public class ReportController implements ReportApiSpecification {
	private final ReportService reportService;

	@PostMapping("/posts/{id}")
	public RsData<PostReportDto> createPostReport(
		@PathVariable long id,
		@RequestBody @Valid ReportCreateRequestDto reportCreateRequestDto,
		Principal principal) {

		PostReportDto postReportDto = reportService.createPostReport(reportCreateRequestDto, principal.getName(), id);

		return RsData.success(HttpStatus.OK, postReportDto);
	}

	@PostMapping("/comments/{id}")
	public RsData<PostCommentReportDto> createPostCommentReport(
		@PathVariable long id,
		@RequestBody @Valid ReportCreateRequestDto reportCreateRequestDto,
		Principal principal) {

		PostCommentReportDto postCommentReportDto = reportService.createPostCommentReport(reportCreateRequestDto, principal.getName(), id);

		return RsData.success(HttpStatus.OK, postCommentReportDto);
	}

	@GetMapping("/posts")
	public RsData<SliceResponse<PostReportResponseDto>> getPostReportList(
		@ModelAttribute @Valid SliceRequest sliceRequest
	){
		SliceResponse<PostReportResponseDto> postReportSlice
			= reportService.getPostReportSlice(sliceRequest.lastId(), sliceRequest.size());

		return RsData.success(HttpStatus.OK, postReportSlice);
	}

	@GetMapping("/comments")
	public RsData<SliceResponse<PostCommentReportResponseDto>> getPostCommentReportList(
		@ModelAttribute @Valid SliceRequest sliceRequest
	){
		SliceResponse<PostCommentReportResponseDto> commentReportSlice
			= reportService.getPostCommentReportSlice(sliceRequest.lastId(), sliceRequest.size());

		return RsData.success(HttpStatus.OK, commentReportSlice);
	}

	@PostMapping("/{reportId}/post/ban")
	public RsData<BanDto> banUserDueToPost(
		@PathVariable long reportId,
		@RequestParam String duration
	){
		BanDto banDto = reportService.banUserDueToPost(reportId, duration);

		return RsData.success(HttpStatus.OK, banDto);
	}

	@PostMapping("/{reportId}/comment/ban")
	public RsData<BanDto> banUserDueToPostComment(
		@PathVariable long reportId,
		@RequestParam String duration
	){
		BanDto banDto = reportService.banUserDueToPostComment(reportId, duration);

		return RsData.success(HttpStatus.OK, banDto);
	}

	@PostMapping("/{reportId}/post/reject")
	public RsData<Void> rejectPostReport(
		@PathVariable long reportId
	){
		reportService.rejectPostReport(reportId);

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/{reportId}/comment/reject")
	public RsData<Void> rejectPostCommentReport(
		@PathVariable long reportId
	){
		reportService.rejectPostCommentReport(reportId);

		return RsData.success(HttpStatus.OK);
	}
}
