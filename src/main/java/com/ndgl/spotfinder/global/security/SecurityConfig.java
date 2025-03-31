package com.ndgl.spotfinder.global.security;

import static org.springframework.security.config.Customizer.*;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ndgl.spotfinder.global.security.jwt.JwtFilter;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SecurityConfig {

	private final TokenProvider tokenProvider;

	/*
	 * 일반 유저용 SecurityFilterChain
	 * */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(withDefaults())
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/v1/users/join",
					"/login/callback",
					"oauth2/**",
					"/api/v1/users/google/login/process"
				).permitAll() // 로그인 경로는 모두 허용
				.requestMatchers(
					"/h2-console/**",
					"/error"
				).permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments").permitAll()
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
					.authenticationEntryPoint((request, response, authException) -> {
						response.setStatus(HttpStatus.UNAUTHORIZED.value());
						response.getWriter().write("로그인이 필요합니다.");
					});
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
}
