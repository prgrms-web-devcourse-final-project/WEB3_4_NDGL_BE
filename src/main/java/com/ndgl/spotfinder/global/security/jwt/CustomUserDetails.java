package com.ndgl.spotfinder.global.security.jwt;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ndgl.spotfinder.domain.user.entity.User;

public class CustomUserDetails implements UserDetails {

	private final User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public String getPassword() {
		// OAuth 사용 시 보통 비밀번호는 없기 때문에 null 또는 빈 문자열 반환
		return "";
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// 권한이 없으면 Collections.emptyList() 반환하거나 기본 권한 부여
		return Collections.singleton(() -> "ROLE_USER");
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public User getUser() {
		return user;
	}

}
