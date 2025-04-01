package com.ndgl.spotfinder.global.security.jwt;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ServiceException;
import com.ndgl.spotfinder.global.security.cookie.TokenCookieUtil;
import com.ndgl.spotfinder.global.security.jwt.service.AdminUserDetailsService;
import com.ndgl.spotfinder.global.security.jwt.service.CustomUserDetailsService;
import com.ndgl.spotfinder.global.security.redis.service.RefreshTokenService;

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

	@Value("${aes.secret.key}")
	private String authorizationKey;

	private SecretKey key;
	private final AdminUserDetailsService adminUserDetailsService;
	private final CustomUserDetailsService customUserDetailsService;
	private final UserRepository userRepository;
	private final TokenCookieUtil tokenCookieUtil;
	private final RefreshTokenService refreshTokenService;

	@PostConstruct
	public void init() {
		// Base64 인코딩된 secret을 디코딩하여 SecretKey 생성
		this.key = new SecretKeySpec(Base64.getDecoder().decode(secret), SignatureAlgorithm.HS512.getJcaName());
	}

	public void createTokenAndSetCookies(Authentication authentication, HttpServletResponse response) {
		if (authentication == null || authentication.getName() == null) {
			log.error("createToken: Authentication 또는 사용자 이름이 null입니다.");
			throw new ServiceException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
		}

		long now = System.currentTimeMillis();
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		String subject = authentication.getName();

		String accessToken = Jwts.builder()
			.setSubject(subject)
			.setExpiration(new Date(now + validationTime))
			.claim("auth", authorities)
			.signWith(this.key, SignatureAlgorithm.HS512)
			.compact();

		String refreshToken = Jwts.builder()
			.setSubject(subject)
			.setExpiration(new Date(now + refreshValidationTime))
			.claim("auth", authorities)
			.signWith(this.key, SignatureAlgorithm.HS512)
			.compact();

		log.info("AccessToken / RefreshToken 생성 완료");

		tokenCookieUtil.setTokenCookies(response, accessToken);
		refreshTokenService.saveRefreshToken(subject, refreshToken);
	}

	//  JWT 토큰 유효성 검증
	public boolean validateToken(String token) {
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

	//  주어진 Access Token의 남은 유효 시간을 밀리초 단위로 반환합니다.
	public Long getExpiration(String accessToken) {
		Date expiration = Jwts.parserBuilder()
			.setSigningKey(key)
			.setAllowedClockSkewSeconds(10)
			.build()
			.parseClaimsJws(accessToken)
			.getBody()
			.getExpiration();
		return expiration.getTime() - System.currentTimeMillis();
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

		return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
	}
}
