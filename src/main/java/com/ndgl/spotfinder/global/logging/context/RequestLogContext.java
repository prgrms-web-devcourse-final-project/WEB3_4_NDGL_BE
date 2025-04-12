package com.ndgl.spotfinder.global.logging.context;

import java.util.Map;

public final class RequestLogContext {
	private RequestLogContext() {
	}

	private static final ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();

	public static void set(Map<String, Object> data) {
		context.set(data);
	}

	public static Map<String, Object> get() {
		return context.get();
	}

	public static void clear() {
		context.remove();
	}
}
