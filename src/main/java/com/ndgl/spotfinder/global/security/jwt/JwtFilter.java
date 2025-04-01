package com.ndgl.spotfinder.global.security.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ndgl.spotfinder.global.app.AppConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final TokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String tokenValue = null;

		// 개발 환경일 경우, Header 인증을 먼저 시도 (for swagger)
		if (AppConfig.isDev()) {
			tokenValue = resolveTokenFromHeader(request);
		}

		// 개발 환경에서 Header 인증이 실패했거나, 배포 환경인 경우 쿠키 인증 시도
		if (tokenValue == null || tokenValue.isEmpty()) {
			tokenValue = resolveTokenFromCookie(request);
		}

		if (StringUtils.hasText(tokenValue) && tokenProvider.validateToken(tokenValue)) {
			Authentication auth = tokenProvider.getAuthentication(tokenValue);
			SecurityContextHolder.getContext().setAuthentication(auth);
		}

		filterChain.doFilter(request, response);
	}

	private String resolveTokenFromHeader(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}


	private String resolveTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null)
			return null;

		for (Cookie cookie : request.getCookies()) {
			if ("accessToken".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}

		return null;
	}
}
