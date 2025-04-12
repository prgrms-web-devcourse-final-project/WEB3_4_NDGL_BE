package com.ndgl.spotfinder.global.common.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class IpAddressUtil {

	private IpAddressUtil(){
	}

	private static final List<String> PROXY_HEADER_NAMES = Arrays.asList(
		"X-Forwarded-For",
		"Proxy-Client-IP",
		"WL-Proxy-Client-IP",
		"HTTP_X_FORWARDED_FOR",
		"HTTP_X_FORWARDED",
		"HTTP_X_CLUSTER_CLIENT_IP",
		"HTTP_CLIENT_IP",
		"HTTP_FORWARDED_FOR",
		"HTTP_FORWARDED",
		"HTTP_VIA",
		"REMOTE_ADDR"
	);

	private static final String UNKNOWN = "unknown";
	private static final String LOCALHOST_IPV4 = "127.0.0.1";
	private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

	public static String getClientIp(HttpServletRequest request) {
		String ip = null;

		// 여러 HTTP 헤더를 순차적으로 확인
		for (String headerName : PROXY_HEADER_NAMES) {
			ip = request.getHeader(headerName);
			if (isValidIp(ip)) {
				break;
			}
		}

		// 헤더에서 IP를 찾지 못한 경우 기본 방법 사용
		if (!isValidIp(ip)) {
			ip = request.getRemoteAddr();
		}

		// X-Forwarded-For 형식에서 첫 번째 IP 추출 (클라이언트 -> 프록시1 -> 프록시2 -> 서버)
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}

		// localhost IPv6 주소를 IPv4로 변환
		if (LOCALHOST_IPV6.equals(ip)) {
			ip = LOCALHOST_IPV4;
		}

		return ip;
	}

	private static boolean isValidIp(String ip) {
		return StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip);
	}
}
