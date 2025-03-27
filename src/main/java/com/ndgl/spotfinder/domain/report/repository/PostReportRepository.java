package com.ndgl.spotfinder.domain.report.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.report.dto.PostReportResponse;
import com.ndgl.spotfinder.domain.report.entity.PostReport;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {
	@Query("SELECT new com.ndgl.spotfinder.domain.report.dto.PostReportResponse(" +
		"pr.post.id, " +
		"pr.reporter.id, " +
		"pr.reportedUser.id, " +
		"pr.reportType, " +
		"pr.reportStatus, " +
		"pr.createdAt) " +
		"FROM PostReport pr ")
	Slice<PostReportResponse> findPostReports(Pageable pageable);
}
