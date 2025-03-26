package com.ndgl.spotfinder.domain.report.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportStatus {
	PENDING("처리 대기"),
	RESOLVED("처리 완료"),
	REJECTED("기각");

	private final String value;
}
