package com.ndgl.spotfinder.domain.comment.service;

import java.util.List;

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
	public SliceResponse<PostCommentDto> getComments(Long postId, Long lastId, int size) {
		List<PostComment> comments;
		if (lastId == null) {
			comments = postCommentRepository.findByPostIdAndParentCommentIsNullOrderByIdDesc(postId);
		} else {
			comments = postCommentRepository.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, lastId);
		}


		boolean hasNext = comments.size() > size;
		if (hasNext) {
			comments = comments.subList(0, size);
		}

		return new SliceResponse<>(
			comments.stream()
				.map(PostCommentDto::new)
				.toList(),
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
	public void write(Long postId, String content, Long parentId) {
		PostComment.PostCommentBuilder commentBuilder = PostComment.builder()
			.userId(1L) // 임시
			.postId(postId)
			.content(content)
			.likeCount(0L);

		if (parentId != null) { // 대댓글 여부
			PostComment parentComment = postCommentRepository.findById(parentId)
				.orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND, "Not found"));
			commentBuilder.parentComment(parentComment);
		}

		postCommentRepository.save(commentBuilder.build());
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
