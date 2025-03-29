package com.ndgl.spotfinder.domain.user.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ndgl.spotfinder.global.base.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "userss",
	uniqueConstraints = {@UniqueConstraint(columnNames = "nickName")})
public class User extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	@NotNull(message = "email 값이 필요합니다.")
	public String email;

	@Setter
	@Column(unique = true, length = 45, nullable = false)
	public String nickName;

	@Setter
	@Column(unique = true, length = 60, nullable = false)
	public String blogName;
}
