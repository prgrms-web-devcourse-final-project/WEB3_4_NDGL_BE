package com.ndgl.spotfinder.global.security.refresh.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.global.security.refresh.entity.RefreshToken;
import com.ndgl.spotfinder.global.security.refresh.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;

	//  refreshToken을 redis에 저장
	public void saveRefreshToken(String accessToken, String token) {
		RefreshToken refreshToken = new RefreshToken(accessToken, token);
		refreshTokenRepository.save(refreshToken);
	}

	//  redis에서 refreshToken 조회
	public Optional<String> getRefreshToken(String email) {
		return refreshTokenRepository.findById(email).map(RefreshToken::getToken);
	}

	//  redis에 존재 여부 확인용 메서드
	public boolean exists(String email) {
		return refreshTokenRepository.existsById(email);
	}

	//  redis에서 refreshToken 삭제
	public void deleteRefreshToken(String email) {
		refreshTokenRepository.deleteById(email);
	}
}
