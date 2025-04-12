package com.ndgl.spotfinder.global.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
	private final Environment environment;

	public boolean isProd() { return environment.matchesProfiles("prod"); }

	public boolean isDev() { return environment.matchesProfiles("dev"); }

	public boolean isTest() {
		return environment.matchesProfiles("test");
	}

	public boolean isNotProd() { return !isProd(); }

	public String getActiveProfile() {
		String[] activeProfiles = environment.getActiveProfiles();
		for (String profile : activeProfiles) {
			if ("prod".equals(profile)) {
				return "prod";
			}
		}
		return "dev"; // prod가 없으면 기본값으로 dev 반환
	}
}