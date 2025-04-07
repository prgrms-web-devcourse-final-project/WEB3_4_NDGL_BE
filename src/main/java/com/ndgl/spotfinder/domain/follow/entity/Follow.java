package com.ndgl.spotfinder.domain.follow.entity;

import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Follow extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "follower_id")
	private User follower;

	@ManyToOne
	@JoinColumn(name = "followee_id")
	private User followee;
}
