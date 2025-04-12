package com.ndgl.spotfinder.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.user.dto.UserInfoResponseDto;
import com.ndgl.spotfinder.domain.user.dto.UserJoinRequestDto;
import com.ndgl.spotfinder.domain.user.dto.UserLoginResponseDto;
import com.ndgl.spotfinder.domain.user.dto.UserModifiedRequestDto;
import com.ndgl.spotfinder.domain.user.dto.UserModifiedResponseDto;
import com.ndgl.spotfinder.domain.user.dto.UserResignedResponseDto;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.service.OauthService;
import com.ndgl.spotfinder.domain.user.service.UserService;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		@Valid @RequestBody UserJoinRequestDto userJoinRequestDTO) {

		userService.join(userJoinRequestDTO);

		return RsData.success(HttpStatus.OK);
	}

	@GetMapping("/google/login/process")
	public RsData<?> processGoogleLogin(
		@RequestParam("code") String code,
		@RequestParam("redirect_uri") String redirectUri,
		HttpServletResponse response
	) {
		//  구글 로그인 처리
		UserLoginResponseDto responseDto = oauthService.processGoogleLogin(code, redirectUri,
			response);

		return new RsData<>(responseDto.getCode(), responseDto.getMessage(), responseDto);

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
	public RsData<UserInfoResponseDto> userInfo(@CookieValue("accessToken") String accessToken) {
		String email = tokenProvider.getEmail(accessToken);
		User user = userService.findUserByEmail(email);

		UserInfoResponseDto targetUser = userService.getUserInfo(user);

		return RsData.success(HttpStatus.OK, targetUser);
	}

	@PutMapping
	public RsData<UserModifiedResponseDto> update(
		@RequestBody UserModifiedRequestDto request,
		@CookieValue("accessToken") String accessToken) {
		String email = tokenProvider.getEmail(accessToken);
		User user = userService.findUserByEmail(email);

		UserModifiedResponseDto response = userService.updateUser(request, user);

		return RsData.success(HttpStatus.OK, response);
	}

	@DeleteMapping("/resign")
	public RsData<UserResignedResponseDto> resign(
		@CookieValue("accessToken") String accessToken,
		HttpServletResponse response) {
		String email = tokenProvider.getEmail(accessToken);
		User user = userService.findUserByEmail(email);

		userService.deleteUser(user, response, accessToken);
		return RsData.success(HttpStatus.OK);
	}
}
