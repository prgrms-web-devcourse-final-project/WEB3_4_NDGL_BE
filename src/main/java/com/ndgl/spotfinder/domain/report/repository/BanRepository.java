package com.ndgl.spotfinder.domain.report.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.report.entity.Ban;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {
	@Query("SELECT b FROM Ban b JOIN b.user u WHERE b.endDate = :today AND u.isBanned = true")
	List<Ban> findBansWithExpiredDate(@Param("today") LocalDate today);
}
