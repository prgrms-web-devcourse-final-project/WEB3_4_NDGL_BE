package com.ndgl.spotfinder.global.security.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException {

		RsData<Void> rsData = RsData.error(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED.getMessage());
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType("application/json; charset=UTF-8");
		objectMapper.writeValue(response.getWriter(), rsData);
	}
}
