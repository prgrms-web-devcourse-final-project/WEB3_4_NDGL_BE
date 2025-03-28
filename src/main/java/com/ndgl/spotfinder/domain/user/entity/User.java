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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Table(name = "users",
	uniqueConstraints = {@UniqueConstraint(columnNames = "nickName")})
public class User extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	@NotNull(message = "email 값이 필요합니다.")
	public String email;

	@Setter
	@NotNull(message = "닉네임 값이 필요합니다.")
	@Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하로 입력해주세요.")
	@Pattern(
		regexp = "^[가-힣a-zA-Z0-9]+$",
		message = "닉네임은 영어, 한글, 숫자만 입력할 수 있습니다."
	)
	@Column(unique = true, length = 45)
	public String nickName;

	@Setter
	@Column(unique = true, length = 60)
	@Size(min = 2, max = 20, message = "블로그 명은 2자 이상 20자 이하로 입력해주세요.")
	@Pattern(
		regexp = "^[가-힣a-zA-Z0-9]+$",
		message = "블로그 명은 영어, 한글, 숫자만 입력할 수 있습니다."
	)
	@NotNull(message = "블로그 명 값이 필요합니다.")
	public String blogName;
}
