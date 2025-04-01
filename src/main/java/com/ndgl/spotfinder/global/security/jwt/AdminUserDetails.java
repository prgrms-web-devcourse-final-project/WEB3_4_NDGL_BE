package com.ndgl.spotfinder.global.security.jwt;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ndgl.spotfinder.domain.admin.entity.Admin;

public class AdminUserDetails implements UserDetails {

	private final Admin admin;

	public AdminUserDetails(Admin admin) {
		this.admin = admin;
	}

	@Override
	public String getUsername() {
		return admin.getUsername();
	}

	@Override
	public String getPassword() {
		return admin.getPassword();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return admin.getAuthorities();
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

	public Admin getAdmin() { return admin; }

}
