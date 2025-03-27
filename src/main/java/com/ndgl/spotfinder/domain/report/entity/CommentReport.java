package com.ndgl.spotfinder.domain.report.entity;

import com.ndgl.spotfinder.domain.report.test.PostComment;
import com.ndgl.spotfinder.domain.report.test.User;
import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CommentReport extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User reporter;

	@JoinColumn(name = "reported_user_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User reportedUser;

	@JoinColumn(name = "comment_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private PostComment postComment;

	@Column(nullable = false)
	private String reason;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportType reportType;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportStatus reportStatus;

	@Builder
	private CommentReport(
		User reporter,
		User reportedUser,
		String reason,
		ReportType reportType,
		PostComment postComment) {
		this.reporter = reporter;
		this.reportedUser = reportedUser;
		this.reason = reason;
		this.reportType = reportType;
		this.reportStatus = ReportStatus.PENDING;
		this.postComment = postComment;
	}
}
