package com.ndgl.spotfinder.domain.report.dto;

public record CommentReportResponse(
	String commentContent,
	ReportDto reportDto) {
}
