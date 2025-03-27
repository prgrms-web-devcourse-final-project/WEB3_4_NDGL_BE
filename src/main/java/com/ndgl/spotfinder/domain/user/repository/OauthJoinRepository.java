package com.ndgl.spotfinder.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.user.entity.Oauth;

import jakarta.validation.constraints.NotNull;

public interface OauthJoinRepository extends JpaRepository<Oauth, Long> {
	Optional<Oauth> findByProviderAndIdentify(Oauth.@NotNull(message = "provider 값이 없습니다. ") Provider provider,
		@NotNull(message = "identify 값이 필요합니다.") String identify);
}
