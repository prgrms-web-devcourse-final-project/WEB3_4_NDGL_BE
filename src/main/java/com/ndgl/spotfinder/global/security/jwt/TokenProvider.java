package com.ndgl.spotfinder.global.security.jwt;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.security.cookie.TokenCookieUtil;
import com.ndgl.spotfinder.global.security.jwt.service.AdminUserDetailsService;
import com.ndgl.spotfinder.global.security.jwt.service.CustomUserDetailsService;
import com.ndgl.spotfinder.global.security.refresh.service.RefreshTokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

	@Value("${jwt.secret.key}")
	private String secret;

	@Value("${jwt.expiration.time}")
	private long validationTime;

	@Value("${jwt.refresh-token.expiration-time}")
	private long refreshValidationTime;

	private SecretKey key;
	private final AdminUserDetailsService adminUserDetailsService;
	private final CustomUserDetailsService customUserDetailsService;
	private final TokenCookieUtil tokenCookieUtil;
	private final RefreshTokenService refreshTokenService;

	@PostConstruct
	public void init() {
		// Base64 인코딩된 secret을 디코딩하여 SecretKey 생성
		this.key = new SecretKeySpec(Base64.getDecoder().decode(secret), SignatureAlgorithm.HS512.getJcaName());
	}

	//  AccessToken 생성
	public String createAccessToken(String email, String authentication) {
		long now = System.currentTimeMillis();

		String accessToken = Jwts.builder()
			.setSubject(email)
			.setExpiration(new Date(now + validationTime))
			.claim("auth", authentication)
			.signWith(this.key, SignatureAlgorithm.HS512)
			.compact();

		return accessToken;
	}

	//  RefreshToken 생성
	public String createRefreshToken(String email, String authentication) {
		long now = System.currentTimeMillis();

		String refreshToken = Jwts.builder()
			.setSubject(email)
			.setExpiration(new Date(now + refreshValidationTime))
			.claim("auth", authentication)
			.signWith(this.key, SignatureAlgorithm.HS512)
			.compact();

		refreshTokenService.saveRefreshToken(email, refreshToken);

		return refreshToken;
	}

	//  로그인 시, accessToken이랑 refreshToken을 같이 생성.
	public void createTokenAndSetCookies(Authentication authentication, HttpServletResponse response) {
		if (authentication == null || authentication.getName() == null) {
			ErrorCode.UNAUTHORIZED.throwServiceException();
		}

		String email = authentication.getName();
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		String accessToken = createAccessToken(email, authorities);
		String refreshToken = createRefreshToken(email, authorities);

		tokenCookieUtil.setTokenCookies(response, accessToken, refreshToken);

		log.info("AccessToken / RefreshToken 생성 완료");
	}

	//  JWT 토큰 유효성 검증
	public boolean validateToken(String token) {
		//  accessToken 및 refreshToken이 없을 때
		if (token == null || token.isEmpty()) {
			return false;
		}

		try {
			Jwts.parserBuilder()
				.setSigningKey(key)
				.setAllowedClockSkewSeconds(10)
				.build().parseClaimsJws(token);
			return true;
		} catch (SecurityException | ExpiredJwtException |
				 UnsupportedJwtException | IllegalArgumentException e) {
			return false;
		}
	}

	//  JWT 토큰을 파싱하여 Claims 객체를 반환
	public Claims parseData(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(this.key)
				.setAllowedClockSkewSeconds(10)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		} catch (Exception e) {
			return null;
		}
	}

	//  토큰에서 권한 추출
	public Authentication getAuthentication(String token) {
		Claims claims = parseData(token); // 유효성 검증 후 claim 추출
		String email = claims.getSubject(); // subject에 있는 이메일 값 추출
		List<SimpleGrantedAuthority> authorities = Arrays.stream(
				claims.get("auth", String.class).split(","))
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList()
			);

		boolean hasRoleAdmin = authorities.stream()
			.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

		UserDetails userDetails = hasRoleAdmin
			? adminUserDetailsService.loadUserByUsername(email)
			: customUserDetailsService.loadUserByUsername(email);

		return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
			userDetails.getAuthorities());
	}

	public String getEmail(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(this.key)
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	public void refreshAccessToken(
		String refreshToken,
		String accessToken,
		HttpServletResponse response
	) {
		String authorities = extractAuthoritiesEvenIfExpired(accessToken);
		String email = getEmailFromTokenEvenIfExpired(accessToken);

		log.info(email);

		boolean isValid = validateToken(refreshToken);

		//  refreshToken 만료 확인
		if (!isValid) {
			refreshToken = createRefreshToken(email, authorities);
		}

		accessToken = createAccessToken(email, authorities);
		tokenCookieUtil.setTokenCookies(response, accessToken, refreshToken);
	}

	public SecretKey getKey() {
		return this.key;
	}

	public long getValidationTime() {
		return this.validationTime;
	}

	//  token 만료 시 만료된 token auth 정보 취득
	public String extractAuthoritiesEvenIfExpired(String token) {
		if (token == null || token.isBlank()) {
			ErrorCode.UNAUTHORIZED.throwServiceException();
		}

		Claims claims;

		try {
			claims = Jwts.parserBuilder()
				.setSigningKey(this.key)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			claims = e.getClaims();
		}

		return claims.get("auth", String.class);
	}

	// token 만료 시 만료된 token에서 email 정보 취득
	public String getEmailFromTokenEvenIfExpired(String token) {
		if (token == null || token.isBlank()) {
			ErrorCode.UNAUTHORIZED.throwServiceException();
		}

		try {
			return Jwts.parserBuilder()
				.setSigningKey(this.key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
		} catch (ExpiredJwtException e) {
			return e.getClaims().getSubject();
		}
	}
}
