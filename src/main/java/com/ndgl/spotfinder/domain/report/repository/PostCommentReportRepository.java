package com.ndgl.spotfinder.domain.report.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.report.dto.PostCommentReportResponse;
import com.ndgl.spotfinder.domain.report.entity.PostCommentReport;

@Repository
public interface PostCommentReportRepository extends JpaRepository<PostCommentReport, Long> {

	@Query("SELECT new com.ndgl.spotfinder.domain.report.dto.PostCommentReportResponse(" +
		"cmt.content, " +
		"cr.reporter.id, " +
		"cr.reportedUser.id, " +
		"cr.reportType, " +
		"cr.reportStatus, " +
		"cr.createdAt) " +
		"FROM PostCommentReport cr " +
		"JOIN cr.postComment cmt " +
		"WHERE cr.id < :lastId")
	Slice<PostCommentReportResponse> findPostCommentReports(long lastId, Pageable pageable);

}
