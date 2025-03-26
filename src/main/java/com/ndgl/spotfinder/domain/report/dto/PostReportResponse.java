package com.ndgl.spotfinder.domain.report.dto;

public record PostReportResponse(
	Long postId,
	ReportDto reportDto
) {
}
