package com.ndgl.spotfinder.global.security;

import static org.springframework.security.config.Customizer.*;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
			.cors(withDefaults())
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
					"/api/*/admin/login",
					"/api/*/admin/join"
				).permitAll() // 로그인 경로는 모두 허용
				.requestMatchers(
					"/h2-console/**",
					"/error"
				).permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments/*").permitAll()
				// 관리자 권한 필요한 요청
				.requestMatchers(HttpMethod.GET, "/api/*/admin/logout").hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/*/admin/resign").hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/*/admin/posts/statistics").hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/*/reports/posts").hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.GET, "/api/*/reports/comments").hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/*/reports/{reportId}/post/ban/{userId}").hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/*/reports/{reportId}/comment/ban/{userId}").hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/*/reports/{reportId}/post/reject").hasAuthority("ROLE_ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/*/reports/{reportId}/comment/reject").hasAuthority("ROLE_ADMIN")
				.anyRequest().authenticated()
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
		configuration.setAllowedOrigins(List.of(
			"https://ndgl.vercel.app",
			"http://localhost:3000")); // ✅ 프론트엔드 주소 허용
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

		configuration.setExposedHeaders(List.of("Authorization", "Refresh-Token"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
