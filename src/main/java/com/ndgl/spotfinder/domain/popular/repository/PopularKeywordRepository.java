package com.ndgl.spotfinder.domain.popular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.popular.entity.PopularKeyword;

@Repository
public interface PopularKeywordRepository extends JpaRepository<PopularKeyword, Long> {
}
