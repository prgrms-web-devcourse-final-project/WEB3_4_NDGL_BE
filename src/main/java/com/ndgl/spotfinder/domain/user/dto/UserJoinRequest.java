package com.ndgl.spotfinder.domain.user.dto;

import com.ndgl.spotfinder.domain.user.entity.Oauth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserJoinRequest {
	@NotNull(message = "provider 값이 없습니다. ")
	private Oauth.Provider provider;

	@NotNull(message = "identify 값이 필요합니다.")
	private String identify;

	@NotNull(message = "email 값이 필요합니다.")
	private String email;

	@NotNull(message = "nickName 값이 필요합니다.")
	@Size(min = 2, max = 15, message = "닉네임은 15자 이하로 입력해주세요.")
	private final String nickName;

	@NotNull(message = "blogName 값이 필요합니다.")
	@Size(min = 2, max = 20, message = "블로그 명은 20자 이하로 입력해주세요.")
	private final String blogName;
}
