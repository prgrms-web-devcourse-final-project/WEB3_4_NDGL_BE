package com.ndgl.spotfinder.global.security.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenCookieUtil {

	@Value("${jwt.expiration.time}")
	private Long validationTime;

	public void setTokenCookies(HttpServletResponse response, String accessToken) {
		//  accessToken을 쿠키에 넣자!
		Cookie accessCookie = new Cookie("accessToken", accessToken);
		accessCookie.setHttpOnly(true);
		//accessCookie.setSecure(true);
		accessCookie.setSecure(false);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(validationTime.intValue() / 1000);// ms -> s로 변환

		response.addCookie(accessCookie);  // accessToken정보 cookie에 등록
	}

	public void cleanTokenCookies(HttpServletResponse response, String cookieName) {
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setHttpOnly(true);  // HttpOnly 속성 추가
		cookie.setSecure(true);  // Secure 속성 추가
		response.addCookie(cookie);
	}
}