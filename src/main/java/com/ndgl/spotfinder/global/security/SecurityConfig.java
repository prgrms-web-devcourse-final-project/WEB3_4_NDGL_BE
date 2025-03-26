package com.ndgl.spotfinder.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SecurityConfig {
	/*
	 * 일반 유저용 SecurityFilterChain
	 * */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/v1/user/login").permitAll() // 로그인 경로는 모두 허용
				.requestMatchers("/api/v1/user/**").authenticated()
			);

		return http.build();
	}
}
