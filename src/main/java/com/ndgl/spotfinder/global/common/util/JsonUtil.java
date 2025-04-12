package com.ndgl.spotfinder.global.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtil {

	private JsonUtil(){}

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static boolean isJsonSerializable(Object obj) {
		try {
			objectMapper.writeValueAsString(obj);
			return true;
		} catch (JsonProcessingException e) {
			return false;
		}
	}
}
