package com.ndgl.spotfinder.domain.post.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.search.document.PostDocument;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostResponseDto(
	@Schema(description = "ID", example = "1")
	Long id,

	@Schema(description = "제목", example = "맛있는 녀석들에 나온 맛집들")
	String title,

	@Schema(description = "내용", example = "TV 예능 맛있는 녀석들에 나온 맛집들입니다.")
	String content,

	@Schema(description = "작성자 이름", example = "맛집사냥꾼")
	String authorName,

	@Schema(description = "썸네일 이미지 URL", example = "https://image.com/images/thumbnail1.jpg")
	String thumbnail,

	@Schema(description = "좋아요 수", example = "50")
	Long likeCount,

	@Schema(description = "댓글 수", example = "72")
	Integer commentCount,

	@Schema(description = "작성 일자", example = "2025-03-23T14:30:00")
	LocalDateTime createdAt,

	@Schema(description = "해시태그 목록")
	List<HashtagDto> hashtags
) {
	public PostResponseDto(Post post) {
		this(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getUser().getNickName(),
			post.getThumbnail(),
			post.getLikeCount(),
			post.getComments().size(),
			post.getCreatedAt(),
			post.getHashtags()
				.stream()
				.limit(3)
				.map(HashtagDto::new)
				.toList()
		);
	}

	public PostResponseDto(PostDocument post) {
		this(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getNickname(),
			post.getThumbnail(),
			post.getLikeCount(),
			0,
			post.getCreatedAt(),
			Optional.ofNullable(post.getHashtags())
				.orElse(Collections.emptyList())
				.stream()
				.limit(3)
				.map(HashtagDto::new)
				.toList()
		);
	}
}
