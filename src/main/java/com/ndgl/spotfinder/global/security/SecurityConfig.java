package com.ndgl.spotfinder.global.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndgl.spotfinder.global.security.handler.CustomAccessDeniedHandler;
import com.ndgl.spotfinder.global.security.handler.CustomAuthenticationEntryPoint;
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
	private final TokenProvider tokenProvider;
	private final AdminUserDetailsService adminUserDetailsService;
	private final CustomAuthenticationSuccessHandler successHandler;
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
					.successHandler(successHandler)
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
				.requestMatchers(
					"/api/v1/users/join",
					"/login/callback",
					"oauth2/**",
					"/api/v1/users/google/login/process",
					"/api/v1/users",
					"/api/v1/users/logout",
					"/api/v1/users/resign",
					"/api/v1/users/info",
					"/api/v1/auth/status",
					"/api/v1/auth/token/refresh",
					"/api/v1/users/google/login/process",
					"/api/*/admin/login",
					"/api/*/admin/join",
					"/api/v1/dev/**"
				)
				.permitAll() // 로그인 경로는 모두 허용
				.requestMatchers(
					"/h2-console/**",
					"/error",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				)
				.permitAll()
				.requestMatchers(HttpMethod.GET,
					"/api/v1/posts/**",
					"/api/v1/posts/*/comments",
					"/api/v1/posts/*/comments/*"
				)
				.permitAll()
				.requestMatchers(HttpMethod.POST,
					"/api/*/reports/posts/{id}",
					"/api/*/reports/comments/{id}"
				)
				.authenticated()
				.requestMatchers(
					"/api/*/admin/**",
					"/api/*/reports/**"
				)
				.hasAuthority("ROLE_ADMIN")
				.anyRequest()
				.authenticated()
			)
			.headers(headers ->
				headers.frameOptions(frameOptions ->
					frameOptions.sameOrigin()
				)
			)
			.addFilterBefore(new JwtFilter(tokenProvider),
				org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
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
