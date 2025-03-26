package com.ndgl.spotfinder.domain.user.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class UserLoginService {
	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;

	@Value("${spring.security.oauth2.client.registration.google.client_secret}")
	private String googleClientSecret;

	@Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
	private String googleRedirectUri;

	public String createGoogleLoginUrl(HttpSession session) {
		String state = UUID.randomUUID().toString();
		session.setAttribute("state", state);

		return "https://accounts.google.com/o/oauth2/auth?"
			+ "response_type=code"
			+ "&client_id=" + googleClientId
			+ "&redirect_uri=" + googleClientSecret
			+ "&scope=email%20profile"
			+ "&state=" + state;
	}

}
