package com.ndgl.spotfinder.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.type.Provider;

public interface OauthRepository extends JpaRepository<Oauth, Long> {
	Optional<Oauth> findByIdentifyAndProvider(String identify, Provider provider);

	//  soft delete용 업데이트 쿼리
	@Modifying
	@Query(
		"UPDATE Oauth o "
		+ "SET o.identify = CONCAT('resigned_', o.id) "
		+ "WHERE o.user.id = :userId"
	)
	int resignOauth(@Param("userId") Long userId);
}
