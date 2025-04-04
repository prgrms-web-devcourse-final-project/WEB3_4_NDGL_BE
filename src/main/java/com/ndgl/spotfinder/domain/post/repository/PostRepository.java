package com.ndgl.spotfinder.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.user.entity.User;

public interface PostRepository extends JpaRepository<Post, Long> {
	@EntityGraph(attributePaths = {"hashtags"})
	Slice<Post> findByIdLessThanOrderByCreatedAtDesc(Long lastId, PageRequest pageRequest);

	@EntityGraph(attributePaths = {"hashtags"})
	Slice<Post> findByUserAndIdLessThanOrderByCreatedAtDesc(User user, Long lastId, PageRequest pageRequest);

	@Query("SELECT p FROM Post p " +
		   "JOIN Like l ON p.id = l.targetId AND l.targetType = 'POST' " +
		   "WHERE l.user.id = :userId AND p.id < :lastId " +
		   "ORDER BY p.createdAt DESC")
	@EntityGraph(attributePaths = {"hashtags"})
	Slice<Post> findLikedPostsByUser(@Param("userId") Long userId, @Param("lastId") Long lastId,
		PageRequest pageRequest);

	Optional<Post> findTopByOrderByIdDesc();

	@Query("SELECT p FROM Post p "
		+ "WHERE (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
		+ "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) "
		+ "OR LOWER(p.user.nickName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
		+ "OR EXISTS (SELECT h FROM p.hashtags h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
		+ "AND p.id > :lastId")
	Slice<Post> searchAll(String keyword, Long lastId, PageRequest pageRequest);
}
