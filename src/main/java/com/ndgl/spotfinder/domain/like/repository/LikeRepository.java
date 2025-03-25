package com.ndgl.spotfinder.domain.like.repository;

import static com.ndgl.spotfinder.domain.like.entity.Like.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.like.entity.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

	// 존재하는 LIKE 확인
	Optional<Like> findByUserIdAndTargetIdAndTargetType(long userId, long targetId, TargetType targetType);

	// LIKE 존재 여부
	boolean existsByUserIdAndTargetIdAndTargetType(long userId, long targetId, TargetType targetType);

	// 특정 사용자가 좋아요한 모든 항목 가져오기
	List<Like> findByUserIdAndTargetType(long userId, TargetType targetType);

	// 특정 대상의 좋아요 수 세기
	long countByTargetIdAndTargetType(long targetId, TargetType targetType);

	// 특정 대상의 전체 좋아요 삭제하기 (예: 포스트가 삭제될 때)
	void deleteByTargetIdAndTargetType(long targetId, TargetType targetType);
}
