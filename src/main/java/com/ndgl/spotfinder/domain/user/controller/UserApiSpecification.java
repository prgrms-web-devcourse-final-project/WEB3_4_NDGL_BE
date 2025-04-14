package com.ndgl.spotfinder.domain.user.controller;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.ndgl.spotfinder.domain.user.dto.UserInfoResponseDto;
import com.ndgl.spotfinder.domain.user.dto.UserJoinRequestDto;
import com.ndgl.spotfinder.domain.user.dto.UserModifiedRequestDto;
import com.ndgl.spotfinder.domain.user.dto.UserModifiedResponseDto;
import com.ndgl.spotfinder.domain.user.dto.UserResignedResponseDto;
import com.ndgl.spotfinder.global.rsdata.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "유저")
public interface UserApiSpecification {

	@Operation(summary = "회원 가입", description = "소셜 플랫폼을 통해 최초 로그인 진행 시 닉네임, 블로그 명을 입력하여 회원가입을 진행.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK")
	})
	RsData<Void> join(@Valid @RequestBody UserJoinRequestDto userJoinRequestDTO);

	@Operation(summary = "구글 로그인 처리", description = "구글 OAuth 인증 코드를 받아 사용자 인증을 수행합니다.")
	RsData<?> processGoogleLogin(
		@Parameter(description = "f/e에서 전해주는 인증 코드") @RequestParam("code") String code,
		@Parameter(description = "f/e에서 전해주는 redirect URI") @RequestParam("redirect_uri") String redirectUri,
		@Parameter(hidden = true) HttpServletResponse response
	);

	@Operation(
		summary = "로그아웃",
		description = "로그아웃 처리 진행.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<Void> logout(
		@CookieValue(value = "accessToken", required = false) String accessToken,
		@Parameter(hidden = true) HttpServletResponse response);

	@Operation(
		summary = "마이페이지",
		description = "마이페이지를 열어 정보를 보여줌.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<UserInfoResponseDto> userInfo(@CookieValue("accessToken") String accessToken);

	@Operation(
		summary = "유저 정보 수정",
		description = "마이페이지를 열어 정보를 보여줌.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<UserModifiedResponseDto> update(
		@RequestBody UserModifiedRequestDto request,
		@CookieValue("accessToken") String accessToken);

	@Operation(
		summary = "회원 탈퇴",
		description = "회원 탈퇴 처리.",
		security = {@SecurityRequirement(name = "JWT")}
	)
	RsData<UserResignedResponseDto> resign(
		@CookieValue("accessToken") String accessToken,
		@Parameter(hidden = true) HttpServletResponse response);
}
