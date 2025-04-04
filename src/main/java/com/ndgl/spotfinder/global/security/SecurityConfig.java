package com.ndgl.spotfinder.global.security;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/v1/users/join",
					"/login/callback",
					"oauth2/**",
					"/api/v1/users/google/login/process",
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
				.requestMatchers(HttpMethod.OPTIONS, "/**")// Preflight 요청(CORS)을 허용하여 브라우저의 사전 요청 차단 문제 해결
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
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
		return factory -> factory.addContextCustomizers(context -> {
			final Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
			cookieProcessor.setSameSiteCookies("None");
			context.setCookieProcessor(cookieProcessor);
		});
	}
}
