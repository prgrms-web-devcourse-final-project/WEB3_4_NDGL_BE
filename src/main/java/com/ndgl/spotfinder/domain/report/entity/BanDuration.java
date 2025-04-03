package com.ndgl.spotfinder.domain.report.entity;

import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BanDuration {
	ONE_DAY("1일"),
	ONE_WEEK("7일"),
	ONE_MONTH("30일"),
	ONE_YEAR("1년"),
	PERMANENT("영구 정지");

	private final String value;

	public static BanDuration fromString(String duration) {
		for (BanDuration banDuration : values()) {
			if (banDuration.value.equals(duration)) {
				return banDuration;
			}
		}
		throw ErrorCode.INVALID_BAN_DURATION.throwServiceException();
	}

}
