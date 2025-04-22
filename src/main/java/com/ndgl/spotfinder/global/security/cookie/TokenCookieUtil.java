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
			domainInCookie = String.format("; Domain=%s;", domain);
		}

		//  cookie에 accessToken 설정
		String accessCookie = buildCookie("accessToken", accessToken, maxAge, domainInCookie, secureFlag, sameSite);

		String refreshCookie = buildCookie("refreshToken", refreshToken, maxAge, domainInCookie, secureFlag, sameSite);

		response.addHeader("Set-Cookie", accessCookie);
		response.addHeader("Set-Cookie", refreshCookie);
	}

	public void cleanTokenCookies(HttpServletResponse response) {

		String secureFlag = secure ? "; Secure" : "";

		String domainInCookie = "";

		if (domain != null && !domain.isEmpty()) {
			domainInCookie = String.format("; Domain=%s;", domain);
		}

		String accessCookie = buildCookie("accessToken", null, 0, domainInCookie, secureFlag, sameSite);
		String refreshCookie = buildCookie("refreshToken", null, 0, domainInCookie, secureFlag, sameSite);

		response.addHeader("Set-Cookie", accessCookie);
		response.addHeader("Set-Cookie", refreshCookie);
	}

	public void refreshAccessTokenCookie(HttpServletResponse response, String accessToken) {
		String secureFlag = secure ? "; Secure" : "";

		String domainInCookie = "";

		if (domain != null && !domain.isEmpty()) {
			domainInCookie = String.format("; Domain=%s;", domain);
		}

		String accessCookie = buildCookie("accessToken", accessToken, -1, domainInCookie, secureFlag, sameSite);

		response.addHeader("Set-Cookie", accessCookie);
	}

	//  쿠키 생성 시 accessToken && refreshToken 생성 및 제거시 이용
	private String buildCookie(String cookieName, String cookieValue, int maxAge, String domain,
		String secure, String sameSite) {
		StringBuilder sb = new StringBuilder();

		if (maxAge >= 0) {
			//  갱신시에는 쿠키의 maxAge가 갱신이 되어서는 안되기 때문에 설정 
			sb.append("; Max-Age=").append(maxAge);
		}

		return new StringBuilder()
			.append(cookieName).append("=").append(cookieValue)
			.append(sb)
			.append("; Path=/")
			.append(domain)
			.append(" HttpOnly")
			.append(secure)
			.append("; SameSite=").append(sameSite)
			.toString();
	}
}