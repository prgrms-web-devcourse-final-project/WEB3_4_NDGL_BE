package com.ndgl.spotfinder.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
}
