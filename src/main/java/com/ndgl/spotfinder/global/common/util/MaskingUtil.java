package com.ndgl.spotfinder.global.common.util;

import com.ndgl.spotfinder.domain.admin.dto.CreateAdminRequest;
import com.ndgl.spotfinder.domain.user.dto.UserJoinRequest;
import com.ndgl.spotfinder.domain.user.dto.UserLoginRequest;

public final class MaskingUtil {

	private MaskingUtil(){
	}

	public static String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return email;
		}

		String[] parts = email.split("@");
		String localPart = parts[0];
		String domainPart = parts[1];

		// 로컬 부분이 3글자 이하인 경우 첫 글자만 남기고 나머지는 마스킹
		if (localPart.length() <= 3) {
			return localPart.charAt(0) +
				"*".repeat(localPart.length() - 1) +
				"@" + domainPart;
		}
		// 로컬 부분이 4글자 이상인 경우 처음과 마지막 글자만 보여주고 나머지 마스킹
		else {
			return localPart.charAt(0) +
				"*".repeat(localPart.length() - 2) +
				localPart.charAt(localPart.length() - 1) +
				"@" + domainPart;
		}
	}

	public static Object maskSensitiveData(Object arg) {
		if (arg instanceof CreateAdminRequest request) {
			return new CreateAdminRequest(
				maskFront(request.username()),
				maskFront(request.password())
			);
		}

		if (arg instanceof UserJoinRequest request) {
			return UserJoinRequest.builder()
				.provider(request.getProvider())
				.identify(maskFront(request.getIdentify()))
				.email(maskEmail(request.getEmail()))
				.nickName(request.getNickName())
				.blogName(request.getBlogName())
				.build();
		}

		if (arg instanceof UserLoginRequest request) {
			return UserLoginRequest.builder()
				.authorizationCode(maskFront(request.getAuthorizationCode()))
				.provider(request.getProvider())
				.build();
		}

		return arg;
	}

	private static String maskFront(String value) {
		if (value == null || value.isBlank()) return value;

		int length = value.length();
		if (length <= 5) {
			return "*".repeat(length);
		}

		String masked = "*****";
		return masked + value.substring(5);
	}
}
