package com.ndgl.spotfinder.domain.comment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentDto;
import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostCommentService {
	private final PostCommentRepository postCommentRepository;

	@Transactional(readOnly = true)
	public SliceResponse<PostCommentDto> getComments(Long postId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

		List<PostComment> comments = postCommentRepository.findByPostIdOrderByIdDesc(postId, pageable);
		boolean hasNext = comments.size() == size;

		return new SliceResponse<>(
			comments.stream()
				.map(PostCommentDto::new)
				.collect(Collectors.toList()),
			hasNext
		);
	}

	private PostComment findCommentAndVerifyPost(Long commentId, Long postId) {
		PostComment comment = postCommentRepository.findById(commentId)
			.orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "Not found"));
		comment.isCommentOfPost(postId);
		return comment;
	}

	@Transactional(readOnly = true)
	public PostCommentDto getComment(Long postId, Long commentId) {
		PostComment comment = findCommentAndVerifyPost(commentId, postId);
		return new PostCommentDto(comment);
	}

	@Transactional
	public void write(Long postId, String content) {
		PostComment comment = PostComment.builder()
			.userId(1L) // 임시
			.postId(postId)
			.content(content)
			.build();
		postCommentRepository.save(comment);
	}

	@Transactional
	public void delete(Long id, Long commentId) {
		PostComment comment = findCommentAndVerifyPost(commentId, id);
		postCommentRepository.delete(comment);
	}

	@Transactional
	public void modify(Long postId, Long commentId, String content) {
		PostComment comment = findCommentAndVerifyPost(commentId, postId);
		comment.setContent(content);
	}
}
