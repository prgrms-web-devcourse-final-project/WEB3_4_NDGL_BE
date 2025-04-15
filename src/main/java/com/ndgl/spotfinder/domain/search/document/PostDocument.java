package com.ndgl.spotfinder.domain.search.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.entity.PostStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(indexName = "post_index")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDocument {
	@Id
	@Field(type = FieldType.Long, index = false)
	private Long id;

	@Field(type = FieldType.Text, analyzer = "post_analyzer")
	private String title;

	@Field(type = FieldType.Text, analyzer = "post_analyzer")
	private String content;

	@Field(type = FieldType.Keyword, index = false)
	private Long userId;

	@Field(type = FieldType.Text, analyzer = "post_analyzer")
	private String nickname;

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime updatedAt;

	@Field(type = FieldType.Keyword, index = false)
	private String thumbnail;

	@Field(type = FieldType.Long)
	private Long viewCount;

	@Field(type = FieldType.Long)
	private Long likeCount;

	@Field(type = FieldType.Integer, index = false)
	private Integer commentCount;

	@Field(type = FieldType.Text, analyzer = "post_analyzer")
	private List<String> hashtags;

	@Field(type = FieldType.Keyword)
	private PostStatus status;

	public static PostDocument from(Post post) {
		return PostDocument.builder()
			.id(post.getId())
			.title(post.getTitle())
			.content(post.getContent())
			.userId(post.getUser().getId())
			.nickname(post.getUser().getNickName())
			.createdAt(post.getCreatedAt())
			.updatedAt(post.getUpdatedAt())
			.thumbnail(post.getThumbnail())
			.viewCount(post.getViewCount())
			.likeCount(post.getLikeCount())
			.commentCount(post.getComments().size())
			.status(post.getStatus())
			.hashtags(
				post.getHashtags().stream()
					.map(Hashtag::getName)
					.toList()
			)
			.build();
	}
}
