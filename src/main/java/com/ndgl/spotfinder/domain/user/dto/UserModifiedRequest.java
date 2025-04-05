package com.ndgl.spotfinder.domain.user.dto;

import com.ndgl.spotfinder.domain.user.entity.User;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserModifiedRequest(
	@NotNull(message = "nickName 값이 필요합니다.")
	@Size(min = 2, max = 15, message = "닉네임은 15자 이하로 입력해주세요.")
	@Pattern(
		regexp = "^[가-힣a-zA-Z0-9]+$",
		message = "닉네임은 영어, 한글, 숫자만 입력할 수 있습니다."
	)
	String nickName,

	@NotNull(message = "blogName 값이 필요합니다.")
	@Size(min = 2, max = 20, message = "블로그 명은 20자 이하로 입력해주세요.")
	@Pattern(
		regexp = "^[가-힣a-zA-Z0-9]+$",
		message = "블로그 명은 영어, 한글, 숫자만 입력할 수 있습니다."
	)
	String blogName
) {
	public static UserModifiedRequest from(User user) {
		return new UserModifiedRequest(
			user.getNickName(),
			user.getBlogName()
		);

	}
}
