package com.ndgl.spotfinder.global.security.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenCookieUtil {

	@Value("${jwt.expiration.time}")
	private Long validationTime;

	public void setTokenCookies(HttpServletResponse response, String accessToken) {
		//  accessToken을 쿠키에 넣자!
		// Cookie accessCookie = new Cookie("accessToken", accessToken);
		// accessCookie.setHttpOnly(true);
		// accessCookie.setSecure(true);
		// //accessCookie.setSecure(false);
		// accessCookie.setPath("/");
		// accessCookie.setMaxAge(validationTime.intValue() / 1000);// ms -> s로 변환

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
		// Cookie cookie = new Cookie(cookieName, null);
		// cookie.setMaxAge(0);
		// cookie.setPath("/");
		// cookie.setHttpOnly(true);  // HttpOnly 속성 추가
		// cookie.setSecure(true);  // Secure 속성 추가
		// response.addCookie(cookie);

		String cookieString = String.format(
			"%s=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=None",
			cookieName
		);

		response.addHeader("Set-Cookie", cookieString);
	}
}