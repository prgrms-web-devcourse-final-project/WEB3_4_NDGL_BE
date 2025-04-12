package com.ndgl.spotfinder.domain.report.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCommentReportResponseDto(
	@Schema(description = "신고 당한 댓글 내용", example = "욕설 댓글")
	String postCommentContent,

	@Schema(description = "신고자 ID", example = "1")
	long reporterId,

	@Schema(description = "피신고자 ID", example = "1")
	long reportedUserId,

	@Schema(description = "신고 유형", example = "욕설")
	String reportType,

	@Schema(description = "신고 상태", example = "처리 대기")
	String reportStatus,

	@Schema(description = "생성 시간", example = "2025-04-08T14:30:00")
	LocalDateTime createdAt) {

	public PostCommentReportResponseDto(
		String postCommentContent,
		long reporterId,
		long reportedUserId,
		ReportType reportType,
		ReportStatus reportStatus,
		LocalDateTime createdAt
	) {
		this(
			postCommentContent,
			reporterId,
			reportedUserId,
			reportType.getValue(),
			reportStatus.getValue(),
			createdAt
		);
	}
}
