package com.ndgl.spotfinder.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAdminRequest(
	@NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
	@Size(min = 4, max = 20, message = "사용자 이름은 4자 이상 20자 이하여야 합니다.")
	String username,

	@NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
	@Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하여야 합니다.")
	String password
) {
}
