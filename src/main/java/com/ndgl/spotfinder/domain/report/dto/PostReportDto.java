package com.ndgl.spotfinder.domain.report.dto;

import com.ndgl.spotfinder.domain.report.entity.PostReport;
import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

public record PostReportDto(
	long id,
	long reporterId,
	long reportedUserId,
	long postId,
	ReportStatus reportStatus,
	ReportType reportType,
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
