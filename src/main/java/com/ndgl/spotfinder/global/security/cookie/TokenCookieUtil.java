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

	public void setTokenCookies(HttpServletResponse response, String accessToken) {
		int maxAge = validationTime.intValue() / 1000;

		String secureFlag = secure ? "; Secure" : "";

		String domainInCookie = "";

		if (domain != null && !domain.isEmpty()) {
			domainInCookie = String.format(" Domain=%s;", domain);
		}

		//  samesite 설정
		String cookieString = String.format(
			"accessToken=%s; Max-Age=%d; Path=/;%s HttpOnly%s; SameSite=%s",
			accessToken,
			maxAge,
			domainInCookie,
			secureFlag,
			sameSite
		);

		response.addHeader("Set-Cookie", cookieString);
	}

	public void cleanTokenCookies(HttpServletResponse response, String cookieName) {

		String secureFlag = secure ? "; Secure" : "";

		String domainInCookie = "";

		if (domain != null && !domain.isEmpty()) {
			domainInCookie = String.format(" Domain=%s;", domain);
		}

		String cookieString = String.format(
			"accessToken=; Max-Age=0; Path=/;%s HttpOnly%s; SameSite=%s",
			domainInCookie,
			secureFlag,
			sameSite
		);

		response.addHeader("Set-Cookie", cookieString);
	}
}