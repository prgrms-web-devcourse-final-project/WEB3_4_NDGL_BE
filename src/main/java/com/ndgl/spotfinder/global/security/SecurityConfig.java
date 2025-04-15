package com.ndgl.spotfinder.global.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.security.handler.CustomAccessDeniedHandler;
import com.ndgl.spotfinder.global.security.handler.CustomAuthenticationEntryPoint;
import com.ndgl.spotfinder.global.security.handler.CustomAuthenticationFailureHandler;
import com.ndgl.spotfinder.global.security.handler.CustomAuthenticationSuccessHandler;
import com.ndgl.spotfinder.global.security.handler.CustomLogoutHandler;
import com.ndgl.spotfinder.global.security.handler.CustomLogoutSuccessHandler;
import com.ndgl.spotfinder.global.security.jwt.JwtFilter;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;
import com.ndgl.spotfinder.global.security.jwt.service.AdminUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;
	private final JwtFilter jwtFilter;
	private final TokenProvider tokenProvider;
	private final AdminUserDetailsService adminUserDetailsService;
	private final CustomAuthenticationSuccessHandler adminAuthSuccessHandler;
	private final CustomAuthenticationFailureHandler adminAuthFailureHandler;
	private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
	private final CustomLogoutHandler customLogoutHandler;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;

	/*
	 * 일반 유저용 SecurityFilterChain
	 * */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
			.formLogin(
				form -> form
					.loginProcessingUrl("/api/*/admin/login")
					.successHandler(adminAuthSuccessHandler)
					.failureHandler(adminAuthFailureHandler)
			)
			.logout(logout -> logout
				.logoutUrl("/api/*/admin/logout")
				.addLogoutHandler(customLogoutHandler)
				.logoutSuccessHandler(customLogoutSuccessHandler)
				.clearAuthentication(true)
			)
			.userDetailsService(adminUserDetailsService)
			.csrf(csrf -> csrf.disable())
			.cors(
				cors -> cors.configurationSource(corsConfigurationSource())
			)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				// 인증
				.requestMatchers(
					"/login/callback",
					"oauth2/**",
					"/api/v1/auth/status",
					"/api/v1/auth/token/refresh"
				)
				.permitAll()

				// 유저
				.requestMatchers(
					"/api/v1/users/join",
					"/api/v1/users/google/login/process",
					"/api/v1/users",
					"/api/v1/users/logout",
					"/api/v1/users/resign",
					"/api/v1/users/info"
				)
				.permitAll()

				// 관리자
				.requestMatchers(
					"/api/*/admin/login",
					"/api/*/admin/join"
				)
				.permitAll()
				.requestMatchers(
					"/api/*/admin/**"
				)
				.hasAuthority("ROLE_ADMIN")

				// 포스트
				.requestMatchers(
					"/api/v1/posts/like",
					"/api/v1/posts/follow"
				)
				.authenticated()
				.requestMatchers(HttpMethod.GET,
					"/api/v1/posts/**"
				)
				.permitAll()

				// 댓글
				.requestMatchers(HttpMethod.GET,
					"/api/v1/posts/*/comments",
					"/api/v1/posts/*/comments/*"
				)
				.permitAll()

				// 신고
				.requestMatchers(
					"/api/*/reports/**"
				)
				.hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.POST,
					"/api/*/reports/posts/{id}",
					"/api/*/reports/comments/{id}"
				)
				.authenticated()

				// 블로그
				.requestMatchers(
					"/api/v1/blogs"
				)
				.permitAll()

				// 기타
				.requestMatchers(
					"/api/v1/dev/**",
					"/h2-console/**",
					"/error",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				)
				.permitAll()
				.anyRequest()
				.authenticated()
			)
			.headers(headers ->
				headers.frameOptions(frameOptions ->
					frameOptions.sameOrigin()
				)
			)
			.addFilterBefore(jwtFilter,
				UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtFilter, LogoutFilter.class)
			.exceptionHandling(exceptionHandling -> {
				exceptionHandling
					.authenticationEntryPoint(customAuthenticationEntryPoint) // 401 에러
					.accessDeniedHandler(customAccessDeniedHandler); // 403 에러
			});

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// 허용할 오리진 설정
		configuration.setAllowedOrigins(Arrays.asList(
			"http://localhost:8080",
			"https://localhost:8080",
			"http://localhost:3000",
			"https://localhost:3000",
			"https://api.ndgl.shop",
			"https://www.ndgl.shop"
		));

		// 허용할 HTTP 메서드 설정
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));

		// 자격 증명 허용 설정 (쿠키 등)
		configuration.setAllowCredentials(true);

		// 허용할 헤더 설정
		configuration.setAllowedHeaders(Arrays.asList("*"));

		// CORS 설정을 특정 경로에 적용
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
