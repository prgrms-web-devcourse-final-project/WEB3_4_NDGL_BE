package com.ndgl.spotfinder.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	Slice<Post> findByIdLessThanOrderByCreatedAtDesc(Long lastId, PageRequest pageRequest);

	Optional<Post> findTopByOrderByIdDesc();
}
