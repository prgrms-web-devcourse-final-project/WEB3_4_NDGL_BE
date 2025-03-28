package com.ndgl.spotfinder.global.security.jwt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.user.entity.Oauth;

public interface OauthReadOnlyRepository extends JpaRepository<Oauth, Long> {
	Optional<Oauth> findByIdentifyAndProvider(String identify, Oauth.Provider provider);
}
