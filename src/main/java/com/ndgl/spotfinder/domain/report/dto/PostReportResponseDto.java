package com.ndgl.spotfinder.domain.report.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostReportResponseDto(
	@Schema(description = "게시물 ID", example = "1")
	long postId,

	@Schema(description = "신고자 ID", example = "1")
	long reporterId,

	@Schema(description = "피신고자 ID", example = "2")
	long reportedUserId,

	@Schema(description = "신고 유형", example = "욕설")
	String reportType,

	@Schema(description = "신고 상태", example = "처리 대기")
	String reportStatus,

	@Schema(description = "생성 시간", example = "2025-04-08T14:30:00")
	LocalDateTime createdAt
) {
	public PostReportResponseDto(
		long postId,
		long reporterId,
		long reportedUserId,
		ReportType reportType,
		ReportStatus reportStatus,
		LocalDateTime createdAt
	) {
		this(
			postId,
			reporterId,
			reportedUserId,
			reportType.getValue(),
			reportStatus.getValue(),
			createdAt
		);
	}
}
