package com.ndgl.spotfinder.global.security.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
		RsData<Void> rsData = RsData.success(HttpStatus.OK);

		response.setStatus(HttpStatus.OK.value());
		response.setContentType("application/json; charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), rsData);
	}
}
