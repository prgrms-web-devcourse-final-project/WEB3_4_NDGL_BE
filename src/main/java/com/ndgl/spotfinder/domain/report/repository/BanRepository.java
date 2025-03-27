package com.ndgl.spotfinder.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.report.entity.Ban;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {
}
