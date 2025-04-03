package com.ndgl.spotfinder.domain.report.dto;

import com.ndgl.spotfinder.domain.report.entity.PostCommentReport;
import com.ndgl.spotfinder.domain.report.entity.ReportStatus;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

public record PostCommentReportDto(
	long id,
	long reporterId,
	long reportedUserId,
	long postCommentId,
	ReportStatus reportStatus,
	ReportType reportType,
	String reason
) {
	public PostCommentReportDto(PostCommentReport postCommentReport) {
		this(
			postCommentReport.getId(),
			postCommentReport.getReporter().getId(),
			postCommentReport.getReportedUser().getId(),
			postCommentReport.getPostComment().getId(),
			postCommentReport.getReportStatus(),
			postCommentReport.getReportType(),
			postCommentReport.getReason()
		);
	}
}

