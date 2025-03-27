package com.ndgl.spotfinder.domain.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	List<PostComment> findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(Long postId, Long lastId);
}
