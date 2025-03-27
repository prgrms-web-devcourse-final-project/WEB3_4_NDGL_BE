package com.ndgl.spotfinder.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.user.dto.UserJoinRequest;
import com.ndgl.spotfinder.domain.user.service.UserJoinService;
import com.ndgl.spotfinder.global.rsdata.RsData;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserJoinController {

	private final UserJoinService userJoinService;

	@PostMapping("/join")
	public RsData<Void> join(
		@Valid @RequestBody UserJoinRequest userJoinRequest) {

		userJoinService.join(userJoinRequest);

		return RsData.success(HttpStatus.OK);
	}

}
