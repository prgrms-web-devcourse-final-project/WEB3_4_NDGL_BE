package com.ndgl.spotfinder.domain.report.dto;

import com.ndgl.spotfinder.domain.report.entity.ReportType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
	@Positive(message = "유저 id 는 양수만 가능합니다.")
	long reportedUserId,

	@NotNull
	ReportType reportType,

	@NotNull
	@Size(max = 100, message = "신고 사유는 최대 100자까지 가능합니다.")
	String reason
) {
}
