package com.ndgl.spotfinder.domain.admin.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.admin.dto.CreateAdminRequest;
import com.ndgl.spotfinder.domain.admin.service.AdminService;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.cookie.TokenCookieUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
	private final AdminService adminService;
	private final TokenCookieUtil tokenCookieUtil;

	@PostMapping("/join")
	public RsData<Void> joinAdmin(@RequestBody @Valid CreateAdminRequest createAdminRequest) {
		adminService.join(createAdminRequest);

		return RsData.success(HttpStatus.OK);
	}

	@PostMapping("/resign")
	public RsData<Void> resignAdmin(Principal principal, HttpServletResponse response) {
		adminService.resign(principal.getName());
		tokenCookieUtil.cleanTokenCookies(response, "accessToken");
		return RsData.success(HttpStatus.OK);
	}

	@GetMapping("/posts/statistics")
	public RsData<Void> getStatistics() {
		return RsData.success(HttpStatus.OK);
	}


}
