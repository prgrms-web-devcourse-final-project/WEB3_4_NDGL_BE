package com.ndgl.spotfinder.domain.image.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.image.dto.ImageRequest;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponse;
import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.global.aws.s3.S3Service;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ActiveProfiles("test")
@SpringBootTest
public class ImageServiceTest {
    
    @InjectMocks
    private ImageService imageService;
    
    @Mock
    private ImageRepository imageRepository;
    
    @Mock
    private S3Service s3Service;
    
    // 테스트 이미지 데이터
    private final Image testImage1 = Image.builder()
            .id(1L)
            .imageType(ImageType.POST)
            .referenceId(10L)
            .url("posts/10/image1.jpg")
            .build();
    
    private final Image testImage2 = Image.builder()
            .id(2L)
            .imageType(ImageType.POST)
            .referenceId(10L)
            .url("posts/10/image2.jpg")
            .build();
    
    @Test
    @DisplayName("Presigned URL 생성 성공")
    public void createImage_success() throws Exception {
        // given
        long postId = 10L;
        List<String> extensions = Arrays.asList("jpg", "png");
        ImageRequest request = new ImageRequest(postId, ImageType.POST, extensions);
        
        List<URL> mockUrls = new ArrayList<>();
        mockUrls.add(new URL("https://example.com/image1.jpg"));
        mockUrls.add(new URL("https://example.com/image2.png"));
        
        // when
        when(s3Service.generatePresignedUrls(ImageType.POST, postId, extensions))
                .thenReturn(mockUrls);
        
        PresignedUrlsResponse response = imageService.createImage(request);
        
        // then
        assertNotNull(response);
        assertEquals(postId, response.id());
        assertEquals(2, response.presignedUrls().size());
        verify(s3Service).generatePresignedUrls(ImageType.POST, postId, extensions);
    }
    
    @Test
    @DisplayName("Presigned URL 생성 실패 - DB 제약조건 위반")
    public void createImage_dbConstraintViolation() throws Exception {
        // given
        long postId = 10L;
        List<String> extensions = Arrays.asList("jpg", "png");
        ImageRequest request = new ImageRequest(postId, ImageType.POST, extensions);
        
        // when
        when(s3Service.generatePresignedUrls(ImageType.POST, postId, extensions))
                .thenThrow(new DataIntegrityViolationException("DB 제약조건 위반"));
        
        // then
        assertThrows(ServiceException.class, () -> imageService.createImage(request));
    }
    
    @Test
    @DisplayName("이미지 조회 및 Presigned URL 반환 성공")
    public void findImagesWithPresignedUrls_success() throws Exception {
        // given
        long postId = 10L;
        List<Image> images = Arrays.asList(testImage1, testImage2);
        
        List<URL> mockUrls = new ArrayList<>();
        mockUrls.add(new URL("https://example.com/image1.jpg"));
        mockUrls.add(new URL("https://example.com/image2.jpg"));
        
        // when
        when(imageRepository.findByImageTypeAndReferenceId(ImageType.POST, postId))
                .thenReturn(images);
        when(s3Service.generatePresignedGetUrl(testImage1.getUrl()))
                .thenReturn(mockUrls.get(0));
        when(s3Service.generatePresignedGetUrl(testImage2.getUrl()))
                .thenReturn(mockUrls.get(1));
        
        PresignedUrlsResponse response = imageService.findImagesWithPresignedUrls(ImageType.POST, postId);
        
        // then
        assertNotNull(response);
        assertEquals(postId, response.id());
        assertEquals(2, response.presignedUrls().size());
        verify(imageRepository).findByImageTypeAndReferenceId(ImageType.POST, postId);
        verify(s3Service, times(2)).generatePresignedGetUrl(anyString());
    }
    
    @Test
    @DisplayName("이미지 저장 성공")
    public void saveImages_success() {
        // given
        long postId = 10L;
        List<String> imageUrls = Arrays.asList(
            "posts/10/image1.jpg",
            "posts/10/image2.jpg"
        );
        
        // when
        imageService.saveImages(ImageType.POST, postId, imageUrls);
        
        // then
        verify(imageRepository).saveAll(anyList());
    }
    
    @Test
    @DisplayName("빈 이미지 URL 목록 저장 시 처리")
    public void saveImages_emptyList() {
        // given
        long postId = 10L;
        List<String> emptyUrls = new ArrayList<>();
        
        // when
        imageService.saveImages(ImageType.POST, postId, emptyUrls);
        
        // then
        verify(imageRepository, never()).saveAll(anyList());
    }
    
    @Test
    @DisplayName("단일 이미지 삭제 성공")
    public void deleteImage_success() {
        // given
        Long imageId = 1L;
        
        // when
        when(imageRepository.findById(imageId)).thenReturn(Optional.of(testImage1));
        
        imageService.deleteImage(imageId);
        
        // then
        verify(s3Service).deleteFile(testImage1.getUrl());
        verify(imageRepository).deleteById(imageId);
    }
    
    @Test
    @DisplayName("존재하지 않는 이미지 삭제 시도")
    public void deleteImage_notFound() {
        // given
        Long imageId = 999L;
        
        // when
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());
        
        // then
        assertThrows(ServiceException.class, () -> imageService.deleteImage(imageId));
        verify(s3Service, never()).deleteFile(anyString());
        verify(imageRepository, never()).deleteById(anyLong());
    }
    
    @Test
    @DisplayName("모든 이미지 삭제 성공")
    public void deletePostWithAllImages_success() {
        // given
        long postId = 10L;
        
        // when
        imageService.deletePostWithAllImages(ImageType.POST, postId);
        
        // then
        verify(imageRepository).deleteAllByImageTypeAndReferenceId(ImageType.POST, postId);
    }
}