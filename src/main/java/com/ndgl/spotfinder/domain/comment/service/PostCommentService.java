package com.ndgl.spotfinder.domain.comment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.dto.PostCommentDto;
import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.post.service.PostService;
import com.ndgl.spotfinder.global.common.dto.SliceResponse;
import com.ndgl.spotfinder.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostCommentService {
	private final PostCommentRepository postCommentRepository;
	private final PostService postService;
	private final PostRepository postRepository;

	@Transactional(readOnly = true)
	public SliceResponse<PostCommentDto> getComments(Long postId, long lastId, int size) {
		List<PostComment> comments =
			postCommentRepository.findByPostIdAndParentCommentIsNullAndIdGreaterThanOrderByIdAsc(postId, lastId);

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
			.orElseThrow(ErrorCode.COMMENT_NOT_FOUND::throwServiceException);
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
		Post post = postRepository.findById(postId)
			.orElseThrow(ErrorCode.POST_NOT_FOUND::throwServiceException);

		PostComment.PostCommentBuilder commentBuilder = PostComment.builder()
			.userId(1L) // 임시
			.post(post)
			.content(content)
			.likeCount(0L);

		if (parentId != null) { // 대댓글 여부
			PostComment parentComment = postCommentRepository.findById(parentId)
				.orElseThrow(ErrorCode.COMMENT_NOT_FOUND::throwServiceException);
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
