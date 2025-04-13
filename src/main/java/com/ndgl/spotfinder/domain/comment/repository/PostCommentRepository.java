package com.ndgl.spotfinder.domain.comment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	Slice<PostComment> findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(
		Long postId, Long lastId, Pageable pageable
	);

	@Query("SELECT c FROM PostComment c WHERE c.post.id = :postId AND c.pinned = true")
	Optional<PostComment> findPinnedCommentByPostId(Long postId);

	@Query("SELECT c FROM PostComment c WHERE c.status = 'DELETED' AND c.childrenComments IS EMPTY")
	List<PostComment> findDeletableComments();
}
