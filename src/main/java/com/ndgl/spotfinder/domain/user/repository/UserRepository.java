package com.ndgl.spotfinder.domain.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ndgl.spotfinder.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByNickName(String nickName);

	Optional<User> findByBlogName(String blogName);

	Slice<User> findAllByIdGreaterThan(Long idIsGreaterThan, Pageable pageable);

	//  soft delete용 업데이트 쿼리
	@Modifying
	@Query("UPDATE User u "
		+ "SET u.isResigned = true,"
		+ "u.email = :maskedEmail "
		+ "WHERE u.id = :userId "
		+ "AND  u.isResigned = false ")
	int resignUser(@Param("userId") Long userId, @Param("maskedEmail") String maskedEmail);
}
