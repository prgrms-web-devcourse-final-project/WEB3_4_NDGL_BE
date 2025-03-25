package com.ndgl.spotfinder.domain.comment.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentDto;
import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostCommentService {
	private final PostCommentRepository postCommentRepository;

	@Transactional(readOnly = true)
	public Slice<PostCommentDto> getComments(Long postId) {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
		Slice<PostComment> comments = postCommentRepository.findByIdLessThanOrderByIdDesc(postId, pageable);

		return comments.map(PostCommentDto::new);
	}

	@Transactional(readOnly = true)
	public PostCommentDto getComment(Long commentId) {
		PostComment comment = postCommentRepository.findById(commentId).orElseThrow();
		return new PostCommentDto(comment);
	}

	@Transactional
	public PostCommentDto write(Long postId, String content) {
		PostComment comment = PostComment.builder()
			.content(content)
			.build();
		postCommentRepository.save(comment);

		return new PostCommentDto(comment);
	}

	@Transactional
	public void delete(Long commentId) {
		postCommentRepository.deleteById(commentId);
	}

	@Transactional
	public void modify(Long commentId, String content) {
		PostComment comment = postCommentRepository.findById(commentId).orElseThrow();
		comment.setContent(content);

		postCommentRepository.save(comment);
	}
}
