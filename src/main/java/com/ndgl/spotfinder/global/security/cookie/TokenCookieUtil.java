package com.ndgl.spotfinder.global.security.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TokenCookieUtil {

	@Value("${app.cookie.secure}")
	private boolean secure;

	@Value("${app.cookie.sameSite}")
	private String sameSite;

	@Value("${app.cookie.domain}")
	private String domain;

	@Value("${jwt.cookie.expiration-time}")
	private Long validationTime;

	public void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
		int maxAge = validationTime.intValue() / 1000;

		String secureFlag = secure ? "; Secure" : "";

		String domainInCookie = "";

		if (domain != null && !domain.isEmpty()) {
			domainInCookie = String.format(" Domain=%s;", domain);
		}

		//  cookie에 accessToken 설정
		String accessCookie = String.format(
			"accessToken=%s; Max-Age=%d; Path=/;%s HttpOnly%s; SameSite=%s",
			accessToken,
			maxAge,
			domainInCookie,
			secureFlag,
			sameSite
		);

		//  cookie에 refreshToken 설정 
		String refreshCookie = String.format(
			"refreshToken=%s; Max-Age=%d; Path=/;%s HttpOnly%s; SameSite=%s",
			refreshToken,
			maxAge,
			domainInCookie,
			secureFlag,
			sameSite
		);

		response.addHeader("Set-Cookie", accessCookie);
		response.addHeader("Set-Cookie", refreshCookie);
	}

	public void cleanTokenCookies(HttpServletResponse response, String cookieName) {

		String secureFlag = secure ? "; Secure" : "";

		String domainInCookie = "";

		if (domain != null && !domain.isEmpty()) {
			domainInCookie = String.format(" Domain=%s;", domain);
		}

		//  cookie에 accessToken 삭제
		String accessCookie = String.format(
			"accessToken=; Max-Age=0; Path=/;%s HttpOnly%s; SameSite=%s",
			domainInCookie,
			secureFlag,
			sameSite
		);

		//  cookie에 refreshToken 삭제
		String refreshToken = String.format(
			"refreshToken=; Max-Age=0; Path=/;%s HttpOnly%s; SameSite=%s",
			domainInCookie,
			secureFlag,
			sameSite
		);

		response.addHeader("Set-Cookie", accessCookie);
		response.addHeader("Set-Cookie", refreshToken);
	}

	public void refreshAccessTokenCookie(HttpServletResponse response, String accessToken) {
		String secureFlag = secure ? "; Secure" : "";

		String domainInCookie = "";

		if (domain != null && !domain.isEmpty()) {
			domainInCookie = String.format(" Domain=%s;", domain);
		}

		//  cookie에 accessToken 설정
		String accessCookie = String.format(
			"accessToken=%s; Path=/;%s HttpOnly%s; SameSite=%s",
			accessToken,
			domainInCookie,
			secureFlag,
			sameSite
		);

		response.addHeader("Set-Cookie", accessCookie);
	}
}