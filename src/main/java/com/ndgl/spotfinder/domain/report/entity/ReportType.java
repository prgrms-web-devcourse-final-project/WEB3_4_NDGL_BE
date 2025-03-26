package com.ndgl.spotfinder.domain.report.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {
	SPAM("스팸"),
	ADVERTISING("광고"),
	IRRELEVANT("주제와 관련 없는 내용"),
	VIOLENCE("폭력적인 내용"),
	PORNOGRAPHY("선정적인 내용"),
	ILLEGAL("불법적인 내용"),
	MISINFORMATION("허위 정보"),
	PRIVACY_VIOLATION("개인정보 침해"),
	COPYRIGHT_INFRINGEMENT("저작권 침해"),
	OTHER("기타");

	private final String value;
}
