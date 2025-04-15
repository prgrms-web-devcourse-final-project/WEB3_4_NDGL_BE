package com.ndgl.spotfinder.domain.report.dto;

import com.ndgl.spotfinder.domain.report.entity.PostCommentReport;
import com.ndgl.spotfinder.domain.report.type.ReportStatus;
import com.ndgl.spotfinder.domain.report.type.ReportType;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCommentReportDto(
	@Schema(description = "ID", example = "1")
	long id,

	@Schema(description = "신고자 ID", example = "1")
	long reporterId,

	@Schema(description = "피신고자 ID", example = "2")
	long reportedUserId,

	@Schema(description = "신고된 댓글 ID", example = "1")
	long postCommentId,

	@Schema(description = "신고 유형", example = "욕설")
	ReportStatus reportStatus,

	@Schema(description = "신고 상태", example = "처리 대기")
	ReportType reportType,

	@Schema(description = "신고 사유", example = "욕설이 포함된 댓글입니다. 제재해주세요.")
	String reason
) {
	public PostCommentReportDto(PostCommentReport postCommentReport) {
		this(
			postCommentReport.getId(),
			postCommentReport.getReporter().getId(),
			postCommentReport.getReportedUser().getId(),
			postCommentReport.getPostComment().getId(),
			postCommentReport.getReportStatus(),
			postCommentReport.getReportType(),
			postCommentReport.getReason()
		);
	}
}
