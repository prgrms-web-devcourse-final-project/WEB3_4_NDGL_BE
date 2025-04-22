package com.ndgl.spotfinder.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.auth.dto.CheckAuthStatusResponseDto;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApiSpecification {

	//  용도 변경
	//  기존에는 accessToken 상테 체크만 진행하지만, 현재는 로그인 되었는지 여부 확인. 이는 jwt필터에서
	//  토큰 체크 후 진행 될 예정.
	@GetMapping("/status")
	public RsData<CheckAuthStatusResponseDto> checkAuthStatus(
		HttpServletResponse response
	) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		boolean isLoggedIn = authentication != null && authentication.isAuthenticated()
			&& !(authentication instanceof AnonymousAuthenticationToken);

		CheckAuthStatusResponseDto responseDto = new CheckAuthStatusResponseDto(isLoggedIn);

		return RsData.success(HttpStatus.OK, responseDto);
	}
}
