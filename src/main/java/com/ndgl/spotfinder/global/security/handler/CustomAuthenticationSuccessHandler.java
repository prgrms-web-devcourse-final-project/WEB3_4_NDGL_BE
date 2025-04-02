package com.ndgl.spotfinder.global.security.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final TokenProvider tokenProvider;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		// 로그인 성공 시 jwt 발급
		tokenProvider.createTokenAndSetCookies(authentication, response);

		RsData<Void> rsData = RsData.success(HttpStatus.OK);
		response.setStatus(HttpStatus.OK.value());
		response.setContentType("application/json; charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), rsData);
	}
}
