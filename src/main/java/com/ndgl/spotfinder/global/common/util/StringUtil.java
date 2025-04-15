package com.ndgl.spotfinder.global.common.util;

public final class StringUtil {
	private StringUtil() {
	}

	public static String removeNewLines(String input) {
		if (input == null)
			return null;
		return input.replaceAll("\\r\\n|\\n|\\r", " ");
	}
}
