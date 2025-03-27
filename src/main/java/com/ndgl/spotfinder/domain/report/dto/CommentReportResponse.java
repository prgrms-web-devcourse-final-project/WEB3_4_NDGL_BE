package com.ndgl.spotfinder.domain.report.dto;

import java.time.LocalDateTime;

import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

public record CommentReportResponse(
	String postCommentContent,
	long reporterId,
	long reportedUserId,
	ReportType reportType,
	ReportStatus reportStatus,
	LocalDateTime createdAt) {
}
