package com.ndgl.spotfinder.global.security.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ndgl.spotfinder.global.app.AppConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final AppConfig appConfig;
	private final TokenProvider tokenProvider;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String tokenValue = null;

		// 개발 환경일 경우, Header 인증을 먼저 시도 (for swagger)
		if (appConfig.isDev()) {
			tokenValue = resolveTokenFromHeader(request);
		}

		// 개발 환경에서 Header 인증이 실패했거나, 배포 환경인 경우 쿠키 인증 시도
		if (tokenValue == null || tokenValue.isEmpty()) {
			tokenValue = resolveTokenFromCookie(request);
		}

		if (StringUtils.hasText(tokenValue) && tokenProvider.validateToken(tokenValue)) {
			log.info("🔐 유효한 accessToken 수신 → 갱신 시도 시작");

			//  refreshToken 취득
			String refreshToken = resolveRefreshTokenFromCookie(request);
			log.info("🍪 refreshToken 추출 결과: {}", refreshToken != null ? "[존재함]" : "[없음]");

			if (StringUtils.hasText(refreshToken)) {
				log.info("🔁 refreshAccessToken 실행");
				String newToken = tokenProvider.refreshAccessToken(refreshToken, tokenValue, response);
				log.info("✅ accessToken 갱신 완료 → newToken: {}", newToken);

				if (tokenProvider.validateToken(newToken)) {
					log.info("🔑 갱신된 accessToken 유효성 확인 완료 → 인증 객체 생성 시도");
					Authentication auth = tokenProvider.getAuthentication(newToken);
					SecurityContextHolder.getContext().setAuthentication(auth);
					log.info("🙆 SecurityContextHolder에 인증 설정 완료 → email: {}", auth.getName());
				} else {
					log.info("❌ 갱신된 accessToken 유효성 실패 → 인증 설정되지 않음");
				}
			} else {
				log.info("⚠️ refreshToken이 존재하지 않아 accessToken 갱신 불가");
			}
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
				String value = cookie.getValue();
				return value.trim();
			}
		}

		return null;
	}

	private String resolveRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null)
			return null;

		for (Cookie cookie : request.getCookies()) {
			if ("refreshToken".equals(cookie.getName())) {
				String value = cookie.getValue();
				return value.trim();
			}
		}

		return null;
	}
}