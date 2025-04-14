package com.ndgl.spotfinder.domain.popular.entity;

import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popular_posts")
@NoArgsConstructor
@Getter
public class PopularPost extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private long postId;

	@Column(nullable = false)
	private long viewCount;

	@Column(nullable = false)
	private int ranking;

	@Builder
	private PopularPost(long postId, long viewCount, int ranking) {
		this.postId = postId;
		this.viewCount = viewCount;
		this.ranking = ranking;
	}
}
