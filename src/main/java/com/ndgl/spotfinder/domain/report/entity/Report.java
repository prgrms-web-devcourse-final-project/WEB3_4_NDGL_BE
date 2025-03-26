package com.ndgl.spotfinder.domain.report.entity;

import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Report extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User reporter;

	@Column(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User reportedUser;

	@Column(nullable = false)
	private String reason;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportType reportType;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportStatus reportStatus;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportTargetType targetType;

	@Column(nullable = false)
	private Long targetId;

	@Builder
	private Report(
		User reporter,
		User reportedUser,
		String reason,
		ReportType reportType,
		ReportTargetType targetType,
		Long targetId) {
		this.reporter = reporter;
		this.reportedUser = reportedUser;
		this.reason = reason;
		this.reportType = reportType;
		this.reportStatus = ReportStatus.PENDING;
		this.targetType = targetType;
		this.targetId = targetId;
	}
}
