package com.ndgl.spotfinder.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.user.dto.UserInfoResponse;
import com.ndgl.spotfinder.domain.user.dto.UserJoinRequest;
import com.ndgl.spotfinder.domain.user.dto.UserLoginResponse;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.OauthService;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final OauthService oauthService;
	private final TokenProvider tokenProvider;
	private final UserService userService;

	public UserController(OauthService oauthService,
		TokenProvider tokenProvider, UserService userService) {
		this.oauthService = oauthService;
		this.tokenProvider = tokenProvider;
		this.userService = userService;
	}

	@PostMapping("/join")
	//  Void 로 바꿀수 없는 이유 >> nickname 및 blogname 입력 시 중복 체크 있음
	//  추가로 request측 입력 체크 때문에 RsData<Void> >> void 변경 시  체크처리 안함.
	public RsData<Void> join(
		@Valid @RequestBody UserJoinRequest userJoinRequest) {

		System.out.println(userJoinRequest);

		userService.join(userJoinRequest);

		return RsData.success(HttpStatus.OK);
	}

	@GetMapping("/google/login/process")
	public RsData<?> processGoogleLogin(
		@RequestParam("code") String code,
		@RequestParam("redirect_uri") String redirectUri,
		HttpServletResponse response
	) {
		//  구글 로그인 처리
		UserLoginResponse responseDto = oauthService.processGoogleLogin(Oauth.Provider.GOOGLE, code, redirectUri,
			response);

		System.out.println(responseDto);

		return RsData.success(HttpStatus.OK, responseDto);

	}

	@PostMapping("/logout")
	public RsData<Void> logout(
		@CookieValue(value = "accessToken", required = false) String accessToken,
		HttpServletResponse response
	) {
		//  accessToken 확인
		if (accessToken == null) {
			ErrorCode.MISSING_ACCESS_TOKEN.throwServiceException();
		}

		String userId = tokenProvider.getEmail(accessToken);

		//  로그아웃 처리
		userService.logout(userId, response, accessToken);

		return RsData.success(HttpStatus.OK);
	}

	@GetMapping("/info")
	public RsData<UserInfoResponse> userInfo(@AuthenticationPrincipal User user) {
		UserInfoResponse targetUser = userService.getUserInfo(user);

		return RsData.success(HttpStatus.OK, targetUser);

	}
}
