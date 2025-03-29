package com.ndgl.spotfinder.domain.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.user.dto.UserJoinRequest;
import com.ndgl.spotfinder.domain.user.dto.UserLoginResponse;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.service.UserJoinService;
import com.ndgl.spotfinder.domain.user.service.UserLoginService;
import com.ndgl.spotfinder.global.exception.ServiceException;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserLoginService userLoginService;
	private final UserJoinService userJoinService;

	public UserController(UserLoginService userLoginService, UserJoinService userJoinService) {
		this.userLoginService = userLoginService;
		this.userJoinService = userJoinService;
	}

	@PostMapping("/join")
	//  Void 로 바꿀수 없는 이유 >> nickname 및 blogname 입력 시 중복 체크 있음
	//  추가로 request측 입력 체크 때문에 RsData<Void> >> void 변경 시  체크처리 안함.
	public RsData<Void> join(
		@Valid @RequestBody UserJoinRequest userJoinRequest) {

		userJoinService.join(userJoinRequest);

		return RsData.success(HttpStatus.OK);
	}

	@GetMapping("/google/login")
	public ResponseEntity<Void> login(HttpSession session) {
		String redirectUrl = userLoginService.createGoogleLoginUrl(session);

		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, redirectUrl).build();
	}

	@GetMapping("/google/login/google/callback")
	public ResponseEntity<Void> googleCallback(
		@RequestParam("code") String code,
		HttpSession session
	) {
		String storedState = (String)session.getAttribute("google_state");

		if (storedState == null) {
			session.removeAttribute("google_state");
			throw new ServiceException(HttpStatus.BAD_REQUEST, "BAD_REQUEST");
		}

		String redirectUrl = "http://localhost:3000/login/callback?provider=GOOGLE&code=" + code;

		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, redirectUrl)
			.build();
	}

	@GetMapping("/google/login/process")
	public ResponseEntity<?> processGoogleLogin(
		@RequestParam("code") String code,
		HttpServletResponse response
	) {
		//  구글 로그인 처리
		UserLoginResponse responseDto = userLoginService.processGoogleLogin(Oauth.Provider.GOOGLE, code, response);

		return ResponseEntity.ok(new RsData<>(responseDto.getCode(), responseDto.getMessage(), responseDto));

	}
}
