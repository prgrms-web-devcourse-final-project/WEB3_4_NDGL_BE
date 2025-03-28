package com.ndgl.spotfinder.global.security.reids.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.global.security.reids.entity.RefreshToken;
import com.ndgl.spotfinder.global.security.reids.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;

	//  refreshToken을 redis에 저장
	public void saveRefreshToken(String email, String token) {
		RefreshToken refreshToken = new RefreshToken(email, token);
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
