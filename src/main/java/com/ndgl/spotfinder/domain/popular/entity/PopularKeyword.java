package com.ndgl.spotfinder.domain.popular.entity;

import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popular_keywords")
@NoArgsConstructor
public class PopularKeyword extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String keyword;

	@Column(nullable = false)
	private long searchCount;

	@Column(nullable = false)
	private int rank;

	@Builder
	private PopularKeyword(String keyword, long searchCount, int rank) {
		this.keyword = keyword;
		this.searchCount = searchCount;
		this.rank = rank;
	}
}
