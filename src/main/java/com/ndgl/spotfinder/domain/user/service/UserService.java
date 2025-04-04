package com.ndgl.spotfinder.domain.user.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.user.dto.UserInfoResponse;
import com.ndgl.spotfinder.domain.user.dto.UserJoinRequest;
import com.ndgl.spotfinder.domain.user.dto.UserJoinResponse;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.OauthRepository;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;
import com.ndgl.spotfinder.global.exception.ErrorCode;
import com.ndgl.spotfinder.global.security.cookie.TokenCookieUtil;
import com.ndgl.spotfinder.global.security.redis.repository.RefreshTokenRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final OauthRepository oauthRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final TokenCookieUtil tokenCookieUtil;

	public User findUserById(long userId) {
		return userRepository.findById(userId)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);
	}

	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(ErrorCode.USER_NOT_FOUND::throwServiceException);
	}

	// 유저 등록
	@Transactional
	public UserJoinResponse join(@Valid UserJoinRequest userJoinRequest) {
		/*
		 *  1.  oauth테이블의 provider, identify 항목 취득 항목 삭제.
		 *    사유 : 지금은 소셜 로그인 플랫폼은 Google 만 이용하는데,
		 *           해당 부분은 추후에 Naver 라던지 Kakao 및 meta 등 소셜 플랫폼이 추가되는 경우
		 *           email 주소 뿐만 아니라 해당 email 주소에 연동 된 provider와 identify 내용이
		 *           존재하는지 파악하기 위한 용도이기 때문에 현재로써는 불필요 하다 판단하여 삭제.
		 * */

		//  1.  users 테이블에 이메일이 존재 하는지 확인.
		Optional<User> existingUser = userRepository.findByEmail(userJoinRequest.getEmail());

		//  2.  Users 테이블에서 같은 닉네임 있는지 확인
		Optional<User> dupNickName = userRepository.findByNickName(userJoinRequest.getNickName());

		//  3.  Users 테이블에서 같은 블로그 명 있는지 확인
		Optional<User> dupBlogName = userRepository.findByBlogName(userJoinRequest.getBlogName());

		//  4.  중복 닉네임 및 블로그 명이 있으면 에러 핸들러 발생
		dupCheck(dupNickName, dupBlogName);

		// 최초 로그인이 아니면 로그인
		if (existingUser.isPresent()) {
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

	public void dupCheck(Optional<User> dupNickName, Optional<User> dupBlogName) {
		if (!dupNickName.isEmpty()) {
			ErrorCode.CONFLICTED_NICKNAME.throwServiceException();
		}

		if (!dupBlogName.isEmpty()) {
			ErrorCode.CONFLICTED_BLOG_NAME.throwServiceException();
		}
	}

	public void logout(String userId, HttpServletResponse response, String accessToken) {
		refreshTokenRepository.deleteById(userId);

		tokenCookieUtil.cleanTokenCookies(response, accessToken);
	}

	public UserInfoResponse getUserInfo(User user) {
		User targerUser = findUserByEmail(user.getEmail());

		return new UserInfoResponse(
			targerUser.getNickName(),
			targerUser.getBlogName(),
			targerUser.getEmail(),
			targerUser.getCreatedAt()
		);
	}
}
