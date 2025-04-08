package com.ndgl.spotfinder.domain.report.dto;

import com.ndgl.spotfinder.domain.report.entity.PostReport;
import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostReportDto(
	@Schema(description = "ID", example = "1")
	long id,

	@Schema(description = "신고자 ID", example = "1")
	long reporterId,

	@Schema(description = "피신고자 ID", example = "2")
	long reportedUserId,

	@Schema(description = "게시물 ID", example = "1")
	long postId,

	@Schema(description = "신고 상태", example = "처리 대기")
	ReportStatus reportStatus,

	@Schema(description = "신고 유형", example = "욕설")
	ReportType reportType,

	@Schema(description = "신고 사유", example = "욕설이 포함된 게시물입니다.")
	String reason
) {
	public PostReportDto(PostReport postReport) {
		this(
			postReport.getId(),
			postReport.getReporter().getId(),
			postReport.getReportedUser().getId(),
			postReport.getPost().getId(),
			postReport.getReportStatus(),
			postReport.getReportType(),
			postReport.getReason()
		);
	}
}
