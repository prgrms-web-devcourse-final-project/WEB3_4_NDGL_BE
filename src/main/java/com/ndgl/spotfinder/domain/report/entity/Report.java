package com.ndgl.spotfinder.domain.report.entity;

import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity

public class Report extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// @Column(nullable = false)
	// @ManyToOne(fetch = FetchType.LAZY)
	// private User reporter;
	//
	// @Column(nullable = false)
	// @ManyToOne(fetch = FetchType.LAZY)
	// private User reportedUser;

	@Column(nullable = false)
	private String reason;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportType reportType;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportStatus reportStatus;

	@Column(nullable = false)
	private Long targetId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportTargetType targetType;

}
