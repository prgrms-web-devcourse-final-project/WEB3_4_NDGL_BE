package com.ndgl.spotfinder.global.util;

import java.util.List;

import org.springframework.core.env.Environment;

public class Ut {

	public static class list {
		public static boolean hasValue(List<?> list) {
			return list != null && !list.isEmpty();
		}
	}

}
