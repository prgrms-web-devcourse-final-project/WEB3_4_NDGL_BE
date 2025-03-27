package com.ndgl.spotfinder.domain.image.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.ndgl.spotfinder.domain.image.entity.Image;

// 게시글에 있는 여러 이미지 정보를 담는 큰 상자
public record PresignedImagesResponse(
    long postId,                           // 게시글 번호
    List<PresignedImageResponse> images    // 여러 이미지의 마법 주문 모음
) {
} 