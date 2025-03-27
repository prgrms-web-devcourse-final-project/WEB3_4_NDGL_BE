package com.ndgl.spotfinder.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.User;

public interface OauthLoginRepository extends JpaRepository<Oauth, Long> {

	Optional<Oauth> findByIdentifyAndProvider(String identify, Oauth.Provider provider);

	Optional<Oauth> findByUserAndProvider(User nowUser, Oauth.Provider provider);
}
