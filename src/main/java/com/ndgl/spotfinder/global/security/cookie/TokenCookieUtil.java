package com.ndgl.spotfinder.global.security.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenCookieUtil {

	@Value("${jwt.expiration.time}")
	private Long validationTime;

	public void setTokenCookies(HttpServletResponse response, String accessToken) {
		int maxAge = validationTime.intValue() / 1000;

		//  samesite 설정
		String cookieString = String.format(
			"accessToken=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=Lax",
			accessToken,
			maxAge
		);

		response.addHeader("Set-Cookie", cookieString);
	}

	public void cleanTokenCookies(HttpServletResponse response, String cookieName) {
		String cookieString = String.format(
			"%s=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=None",
			cookieName
		);

		response.addHeader("Set-Cookie", cookieString);
	}
}