package com.ndgl.spotfinder.global.common.util;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public class CommonUtil {

	public static class list {
		public static boolean hasValue(List<?> list) {
			return list != null && !list.isEmpty();
		}
	}

	public static String getEmail(Principal principal) {
		return Optional.ofNullable(principal)
			.map(Principal::getName)
			.orElse(null); // 익명 사용자
	}

}
