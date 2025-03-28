package com.ndgl.spotfinder.global.security.jwt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.user.entity.User;

@Repository
public interface UserReadOnlyRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
}
