package com.ndgl.spotfinder.domain.report.dto;

import java.time.LocalDate;

import com.ndgl.spotfinder.domain.report.entity.Ban;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;

public record BanDto(
	@Schema(description = "ID", example = "1")
	long id,

	@Schema(description = "제재 대상 유저 ID", example = "1")
	long userId,

	@Schema(description = "제재 시작 날짜", example = "2025-04-03")
	LocalDate startDate,

	@Schema(description = "제재 종료 날짜", example = "2025-04-10")
	LocalDate endDate,

	@Schema(description = "제재 유형", example = "욕설")
	ReportType banType
) {
	public BanDto(Ban ban) {
		this(
			ban.getId(),
			ban.getUser().getId(),
			ban.getStartDate(),
			ban.getEndDate(),
			ban.getBanType()
		);
	}
}
