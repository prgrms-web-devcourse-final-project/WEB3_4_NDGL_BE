package com.ndgl.spotfinder.domain.user.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.user.dto.UserJoinRequest;
import com.ndgl.spotfinder.domain.user.dto.UserJoinResponse;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.OauthRepository;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ServiceException;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserJoinService {
	//private final UserJoinRepository userJoinRepository;
	//private final OauthJoinRepository oauthJoinRepository;
	private final UserRepository userRepository;
	private final OauthRepository oauthRepository;

	// 유저 등록
	@Transactional
	public UserJoinResponse join(@Valid UserJoinRequest userJoinRequest) {
		//  1.  oauth테이블의 provider, identify 항목 취득
		Optional<Oauth> existingOauths = oauthRepository.findByProviderAndIdentify(
			userJoinRequest.getProvider(),
			userJoinRequest.getIdentify()
		);

		//  2.  users 테이블에 이메일이 존재 하는지 확인.
		Optional<User> existingUser = userRepository.findByEmail(userJoinRequest.getEmail());

		//  3.  Users 테이블에서 같은 닉네임 있는지 확인
		Optional<User> dupNickName = userRepository.findByNickName(userJoinRequest.getNickName());

		//  4.  Users 테이블에서 같은 블로그 명 있는지 확인
		Optional<User> dupBlogName = userRepository.findByBlogName(userJoinRequest.getBlogName());

		//  중복 닉네임 및 블로그 명이 있으면 에러 핸들러 발생
		if (!dupNickName.isEmpty()) {
			throw new ServiceException(HttpStatus.CONFLICT, "이미 사용중인 닉네임 입니다.");
		}

		if (!dupBlogName.isEmpty()) {
			throw new ServiceException(HttpStatus.CONFLICT, "이미 사용중인 블로그 명 입니다.");
		}

		// 최초 로그인이 아니면 로그인
		if (existingOauths.isPresent() && existingUser.isPresent()) {
			return UserJoinResponse.builder()
				.code(HttpStatus.OK.value())
				.message("OK")
				.build();
		} else {
			User newUser = User.builder()
				.email(userJoinRequest.getEmail())
				.nickName(userJoinRequest.getNickName())
				.blogName(userJoinRequest.getBlogName())
				.build();
			userRepository.save(newUser);

			Oauth newOauth = Oauth.builder()
				.user(newUser)
				.provider(userJoinRequest.getProvider())
				.identify(userJoinRequest.getIdentify())
				.build();
			oauthRepository.save(newOauth);

			return UserJoinResponse.builder()
				.message("ok")
				.code(HttpStatus.OK.value())
				.build();
		}
	}
}
