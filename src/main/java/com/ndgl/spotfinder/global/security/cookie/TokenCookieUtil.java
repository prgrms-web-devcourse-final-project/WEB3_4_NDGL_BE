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
		String accessCookie = new StringBuilder()
			.append("accessToken=").append(accessToken)
			.append("; Max-Age=").append(maxAge)
			.append("; Path=/")
			.append(domainInCookie) // 이건 이미 "; Domain=..." 형식이므로 ; 생략
			.append("; HttpOnly")
			.append(secureFlag)
			.append("; SameSite=").append(sameSite)
			.toString();

		//  cookie에 refreshToken 설정 
		String refreshCookie = new StringBuilder()
			.append("refreshToken=").append(refreshToken)
			.append("; Max-Age=").append(maxAge)
			.append("; Path=/")
			.append(domainInCookie) // 이건 이미 "; Domain=..." 형식이므로 ; 생략
			.append("; HttpOnly")
			.append(secureFlag)
			.append("; SameSite=").append(sameSite)
			.toString();

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
		String accessCookie = new StringBuilder()
			.append("accessToken=")
			.append("; Max-Age=").append(0)
			.append("; Path=/")
			.append(domainInCookie) // 이건 이미 "; Domain=..." 형식이므로 ; 생략
			.append("; HttpOnly")
			.append(secureFlag)
			.append("; SameSite=").append(sameSite)
			.toString();

		//  cookie에 refreshToken 삭제
		String refreshToken = new StringBuilder()
			.append("refreshToken=")
			.append("; Max-Age=").append(0)
			.append("; Path=/")
			.append(domainInCookie) // 이건 이미 "; Domain=..." 형식이므로 ; 생략
			.append("; HttpOnly")
			.append(secureFlag)
			.append("; SameSite=").append(sameSite)
			.toString();

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
		String accessCookie = new StringBuilder()
			.append("accessToken=").append(accessToken)
			.append("; Path=/")
			.append(domainInCookie) // 이건 이미 "; Domain=..." 형식이므로 ; 생략
			.append("; HttpOnly")
			.append(secureFlag)
			.append("; SameSite=").append(sameSite)
			.toString();

		response.addHeader("Set-Cookie", accessCookie);
	}
}