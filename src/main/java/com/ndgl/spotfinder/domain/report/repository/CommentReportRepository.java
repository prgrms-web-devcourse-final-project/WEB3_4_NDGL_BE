package com.ndgl.spotfinder.domain.report.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.report.dto.CommentReportResponse;
import com.ndgl.spotfinder.domain.report.entity.CommentReport;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

	@Query("SELECT new com.ndgl.spotfinder.domain.report.dto.CommentReportResponse(" +
		"cmt.content, " +
		"cr.reporter.id, " +
		"cr.reportedUser.id, " +
		"cr.reportType, " +
		"cr.reportStatus, " +
		"cr.createdAt) " +
		"FROM CommentReport cr " +
		"JOIN cr.postComment cmt")
	Slice<CommentReportResponse> findCommentReports(Pageable pageable);

}
