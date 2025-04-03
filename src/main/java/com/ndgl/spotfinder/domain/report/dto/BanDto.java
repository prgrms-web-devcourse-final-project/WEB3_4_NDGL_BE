package com.ndgl.spotfinder.domain.report.dto;

import java.time.LocalDate;

import com.ndgl.spotfinder.domain.report.entity.Ban;
import com.ndgl.spotfinder.domain.report.entity.ReportType;

public record BanDto(
	long id,
	long userId,
	LocalDate startDate,
	LocalDate endDate,
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
