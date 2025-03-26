package com.ndgl.spotfinder.domain.report.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.report.entity.Report;
import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

public record ReportDto(
	long reporterId,
	long reportedUserId,
	ReportType reportType,
	ReportStatus reportStatus,
	LocalDateTime createdAt
) {
	public ReportDto(Report report){
		this(report.getReporter().getId(),
			report.getReportedUser().getId(),
			report.getReportType(),
			report.getReportStatus(),
			report.getCreatedAt());
	}
}
