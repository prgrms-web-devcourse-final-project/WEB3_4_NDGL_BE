package com.ndgl.spotfinder.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByNickName(String nickName);

	Optional<User> findByBlogName(String blogName);

}
