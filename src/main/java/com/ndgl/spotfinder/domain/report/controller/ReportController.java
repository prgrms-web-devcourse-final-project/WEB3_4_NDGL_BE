package com.ndgl.spotfinder.domain.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.report.dto.ReportCreateRequest;
import com.ndgl.spotfinder.domain.report.entity.ReportTargetType;
import com.ndgl.spotfinder.domain.report.service.ReportService;
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
		@Valid @RequestBody ReportCreateRequest reportCreateRequest) {
		// SpringSecurity Context 에서 User 획득
		long reporterId = 1;

		reportService.createReport(reportCreateRequest, reporterId, ReportTargetType.POST, id);

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/comments/{id}")
	@Operation(summary = "댓글 신고 요청")
	public RsData<Void> reportComment(
		@PathVariable Long id,
		@Valid @RequestBody ReportCreateRequest reportCreateRequest) {
		// SpringSecurity Context 에서 User 획득
		long reporterId = 1;

		reportService.createReport(reportCreateRequest, reporterId, ReportTargetType.COMMENT, id);

		return RsData.success(HttpStatus.OK);
	}
}
