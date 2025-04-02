package com.ndgl.spotfinder.global.dev;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.rsdata.RsData;
import com.ndgl.spotfinder.global.security.jwt.TokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
@Profile("dev")
@Tag(name = "개발 도구", description = "개발 환경에서만 사용 가능한 API")
public class DevAuthController {

	private final TokenProvider tokenProvider;
	private final UserRepository userRepository;

	@GetMapping("/token/{userId}")
	@Operation(summary = "개발용 JWT 토큰 발급", description = "Swagger UI 테스트를 위한 JWT 토큰을 발급합니다")
	public RsData getTestToken(
		@Parameter(description = "유저 ID", example = "1") @PathVariable Long userId
	) {
		User user = userRepository.findById(userId)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			user.getEmail(),
			"",
			Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
		);

		long now = System.currentTimeMillis();
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(java.util.stream.Collectors.joining(","));

		String token = io.jsonwebtoken.Jwts.builder()
			.setSubject(user.getEmail())
			.setExpiration(new java.util.Date(now + tokenProvider.getValidationTime()))
			.claim("auth", authorities)
			.signWith(tokenProvider.getKey(), io.jsonwebtoken.SignatureAlgorithm.HS512)
			.compact();

		Map<String, String> response = new HashMap<>();
		response.put("token", token);
		response.put("userId", userId.toString());
		response.put("email", user.getEmail());
		response.put("message", "이 토큰을 Swagger UI의 Authorize 버튼에 입력하세요 (Bearer 접두사 없이)");

		return RsData.success(HttpStatus.OK, response);
	}
}