package com.ndgl.spotfinder.domain.report.dto;

import com.ndgl.spotfinder.domain.report.entity.ReportType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
	@NotNull
	ReportType reportType,

	@NotBlank
	@Size(max = 100, message = "신고 사유는 최대 100자까지 가능합니다.")
	String reason
) {
}
