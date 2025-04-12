package com.ndgl.spotfinder.domain.admin.controller;

import java.security.Principal;

import com.ndgl.spotfinder.domain.admin.dto.AdminCreateRequestDto;
import com.ndgl.spotfinder.domain.admin.dto.AdminCreateResponseDto;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "관리자")
public interface AdminApiSpecification {

	@Operation(summary = "관리자 가입")
	RsData<AdminCreateResponseDto> joinAdmin(
		AdminCreateRequestDto adminCreateRequestDto
	);

	@Operation(
		summary = "관리자 탈퇴",
		responses = {
		@ApiResponse(responseCode = "200", description = "성공", content = @Content(
			mediaType = "application/json",
			examples = @ExampleObject("{\"code\": 200, \"message\": \"OK\"}")
		))
	})
	RsData<Void> resignAdmin(
		@Parameter(hidden = true) Principal principal,
		@Parameter(hidden = true) HttpServletResponse response
	);
}
