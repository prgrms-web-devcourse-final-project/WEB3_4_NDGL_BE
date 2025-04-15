package com.ndgl.spotfinder.domain.post.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.type.PostStatus;
import com.ndgl.spotfinder.domain.user.entity.User;

public interface PostRepository extends JpaRepository<Post, Long> {
	Slice<Post> findByStatusAndIdLessThanOrderByCreatedAtDesc(
		PostStatus status, Long lastId, PageRequest pageRequest);

	Slice<Post> findByStatusAndUserAndIdLessThanOrderByCreatedAtDesc(
		PostStatus status, User user, Long lastId, PageRequest pageRequest);

	@Query("SELECT p FROM Post p " +
		"JOIN Like l ON p.id = l.targetId AND l.targetType = 'POST' " +
		"WHERE l.user.id = :userId AND p.id < :lastId AND p.status = :postStatus " +
		"ORDER BY p.createdAt DESC")
	Slice<Post> findLikedPostsByUser(
		@Param("userId") Long userId,
		@Param("lastId") Long lastId,
		@Param("postStatus") PostStatus postStatus,
		PageRequest pageRequest);

	@Query("SELECT p FROM Post p " +
		"JOIN Follow f ON f.follower.id = :userId AND f.followee.id = p.user.id " +
		"WHERE p.id < :lastId " +
		"AND p.status = :postStatus " +
		"ORDER BY p.createdAt DESC")
	Slice<Post> findFollowedPostsByUser(
		@Param("userId") Long userId,
		@Param("lastId") Long lastId,
		@Param("postStatus") PostStatus postStatus,
		PageRequest pageRequest);

	Optional<Post> findTopByOrderByIdDesc();

	Optional<Post> findFirstByUserAndStatus(User user, PostStatus status);

	@Query("SELECT p FROM Post p "
		+ "WHERE (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
		+ "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) "
		+ "OR LOWER(p.user.nickName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
		+ "OR EXISTS (SELECT h FROM p.hashtags h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
		+ "AND p.status = 'PUBLIC'"
		+ "ORDER BY p.createdAt DESC")
	Slice<Post> searchAll(String keyword, PageRequest pageRequest);

	List<Post> findByUser(User user);

	@Query("SELECT DISTINCT p FROM Post p "
		+ "JOIN FETCH p.user "
		+ "LEFT JOIN FETCH p.comments "
		+ "LEFT JOIN FETCH p.hashtags "
		+ "LEFT JOIN FETCH p.locations "
		+ "WHERE p.status = 'PUBLIC'")
	List<Post> findAllWithAssociations();

	@Query("SELECT DISTINCT p FROM Post p "
		+ "JOIN FETCH p.user "
		+ "LEFT JOIN FETCH p.hashtags "
		+ "WHERE p.status = :status")
	List<Post> findAllWithAssociations(@Param("status") PostStatus status);

	@Query("SELECT DISTINCT p FROM Post p "
		+ "JOIN FETCH p.user LEFT JOIN FETCH p.hashtags "
		+ "WHERE p.updatedAt > :updatedAt "
		+ "AND p.status = 'PUBLIC'")
	List<Post> findByUpdatedAtAfter(LocalDateTime updatedAt);

	@Query("SELECT DISTINCT p FROM Post p "
		+ "JOIN FETCH p.user LEFT JOIN FETCH p.hashtags "
		+ "WHERE p.updatedAt > :updatedAt "
		+ "AND p.status = :status")
	List<Post> findByUpdatedAtAfter(@Param("status") PostStatus status, LocalDateTime updatedAt);

	@Modifying
	@Query("UPDATE Post p SET p.viewCount = p.viewCount + :count WHERE p.id = :postId")
	void incrementViewCount(@Param("postId") Long postId, @Param("count") Long count);

	List<Post> findAllByStatusAndDeleteScheduledAt(PostStatus status, LocalDate deleteScheduledAt);
}
