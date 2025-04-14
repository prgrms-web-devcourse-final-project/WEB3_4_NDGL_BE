package com.ndgl.spotfinder.global.common.util;

import org.jsoup.Jsoup;

public final class MarkdownUtil {
	private MarkdownUtil() {
	}

	public static String extractTextFromMarkdown(String markdownText) {
		return Jsoup.parse(markdownText).text();
	}
}
