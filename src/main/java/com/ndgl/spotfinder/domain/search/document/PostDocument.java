package com.ndgl.spotfinder.domain.search.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(indexName = "my_index")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDocument {
	@Id
	@Field(type = FieldType.Long, index = false)
	private Long id;

	@Field(type = FieldType.Text, analyzer = "test_analyzer")
	private String title;

	@Field(type = FieldType.Text, analyzer = "test_analyzer")
	private String content;

	@Field(type = FieldType.Keyword, index = false)
	private String nickname;

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
	private LocalDateTime createdAt;

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
	private LocalDateTime updatedAt;

	@Field(type = FieldType.Keyword, index = false)
	private String thumbnail;

	@Field(type = FieldType.Long, index = false)
	private Long viewCount;

	@Field(type = FieldType.Long, index = false)
	private Long likeCount;

	@Field(type = FieldType.Keyword)
	private List<String> hashtags;

	public static PostDocument from(Post post) {
		return PostDocument.builder()
			.id(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.nickname(post.getUser().getNickName())
			.createdAt(post.getCreatedAt())
			.updatedAt(post.getUpdatedAt())
			.thumbnail(post.getThumbnail())
			.viewCount(post.getViewCount())
			.likeCount(post.getLikeCount())
			.hashtags(
				post.getHashtags().stream()
					.map(Hashtag::getName)
					.toList()
			)
			.build();
	}
}
