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

	@Value("${jwt.expiration.time}")
	private Long validationTime;

	public void setTokenCookies(HttpServletResponse response, String accessToken) {
		int maxAge = validationTime.intValue() / 1000;

		String secureFlag = secure ? "; Secure" : "";
		String domainFlag = (domain != null && !domain.isBlank()) ? "; Domain=" + domain : "";

		log.info("secureFlag: " + secureFlag);
		log.info("domainFlag: " + domainFlag);

		//  samesite 설정
		String cookieString = String.format(
			"accessToken=%s; Max-Age=%d; Path=/;%s%s HttpOnly; SameSite=%s",
			accessToken,
			maxAge,
			domainFlag,
			secureFlag,
			sameSite
		);

		response.addHeader("Set-Cookie", cookieString);
	}

	public void cleanTokenCookies(HttpServletResponse response, String cookieName) {
		String cookieString = String.format(
			"%s=; Max-Age=0; Path=/; Domain=%s; HttpOnly; SameSite=%s",
			cookieName,
			domain,
			sameSite
		);

		response.addHeader("Set-Cookie", cookieString);
	}
}