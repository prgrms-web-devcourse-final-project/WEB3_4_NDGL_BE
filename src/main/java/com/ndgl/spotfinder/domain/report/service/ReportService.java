package com.ndgl.spotfinder.domain.report.service;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.report.dto.ReportCreateRequest;
import com.ndgl.spotfinder.domain.report.entity.Report;
import com.ndgl.spotfinder.domain.report.entity.ReportTargetType;
import com.ndgl.spotfinder.domain.report.repository.ReportRepository;
import com.ndgl.spotfinder.domain.user.entity.User;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final EntityManager entityManager;

	public void createReport(
		ReportCreateRequest reportCreateRequest,
		Long reporterId,
		ReportTargetType reportTargetType,
		Long targetId) {
		User reporter = entityManager.getReference(User.class, reporterId);
		User reportedUser = entityManager.getReference(User.class, reportCreateRequest.reportedUserId());

		Report report = Report.builder()
			.reporter(reporter)
			.reportedUser(reportedUser)
			.reason(reportCreateRequest.reason())
			.reportType(reportCreateRequest.reportType())
			.targetType(reportTargetType)
			.targetId(targetId)
			.build();

		reportRepository.save(report);
	}

}
