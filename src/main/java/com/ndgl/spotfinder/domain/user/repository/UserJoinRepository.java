package com.ndgl.spotfinder.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.user.entity.User;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public interface UserJoinRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByNickName(
		@NotNull(message = "nickName 값이 필요합니다.") @Size(min = 2, max = 15, message = "닉네임은 15자 이하로 입력해주세요.") String nickName);

	Optional<User> findByBlogName(
		@Size(min = 2, max = 20, message = "블로그 명은 20자 이하로 입력해주세요.") @NotNull(message = "blogName 값이 필요합니다.") String blogName);
}
