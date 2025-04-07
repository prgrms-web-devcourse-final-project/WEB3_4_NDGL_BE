package com.ndgl.spotfinder.domain.image.service;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.global.aws.s3.S3Service;

@SpringBootTest
@ActiveProfiles("test")
public class ImageCleanupServiceTest {

    @Autowired
    private ImageCleanupService imageCleanupService;

    @MockitoBean
    private ImageRepository imageRepository;

    @MockitoBean
    private S3Service s3Service;

    @Captor
    private ArgumentCaptor<Image> imageCaptor;

    private List<Image> mockImages;

    @BeforeEach
    void setUp() {
        Image image1 = Image.builder()
            .id(1L)
            .imageType(ImageType.POST)
            .referenceId(100L)
            .url("https://example.com/image1.jpg")
            .build();

        Image image2 = Image.builder()
            .id(2L)
            .imageType(ImageType.POST)
            .referenceId(100L)
            .url("https://example.com/image2.jpg")
            .build();

        Image image3 = Image.builder()
            .id(3L)
            .imageType(ImageType.POST)
            .referenceId(100L)
            .url("https://example.com/image3.jpg")
            .build();

        mockImages = Arrays.asList(image1, image2, image3);

        // Mock 설정
        when(imageRepository.findByImageTypeAndReferenceId(ImageType.POST, 100L))
                .thenReturn(mockImages);
    }

    @Test
    public void testAsyncImageCleanupExecution() {
        // Given
        ImageType imageType = ImageType.POST;
        long referenceId = 100L;
        
        // 사용 중인 이미지 URL (image1만 사용 중)
        Set<String> usedImageUrls = new HashSet<>();
        usedImageUrls.add("https://example.com/image1.jpg");

        // 스레드 이름 확인용 변수
        final AtomicBoolean asyncExecuted = new AtomicBoolean(false);
        final String[] asyncThreadName = new String[1];

        // S3 서비스 모의 설정
        doAnswer(invocation -> {
            asyncThreadName[0] = Thread.currentThread().getName();
            asyncExecuted.set(true);
            return null;
        }).when(s3Service).deleteFile(anyString());

        // When
        System.out.println("메인 스레드: " + Thread.currentThread().getName());
        imageCleanupService.cleanupUnusedImages(imageType, referenceId, usedImageUrls);
        System.out.println("비동기 메서드 호출 직후 - 메인 스레드는 계속 진행");

        // Then
        // 1. 비동기 메서드가 다른 스레드에서 실행되는지 확인
        await().atMost(5, TimeUnit.SECONDS).untilTrue(asyncExecuted);
        
        // 2. 다른 스레드에서 실행되었는지 확인
        System.out.println("비동기 스레드: " + asyncThreadName[0]);
        assertTrue(asyncThreadName[0].startsWith("ImageClean-"));
        
        // 3. 적절한 이미지가 삭제되었는지 확인
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(s3Service, times(2)).deleteFile(anyString());
            verify(imageRepository, times(2)).delete(imageCaptor.capture());
            
            List<Image> deletedImages = imageCaptor.getAllValues();
            assertEquals(2, deletedImages.size());
            assertTrue(deletedImages.stream()
                .anyMatch(img -> img.getUrl().equals("https://example.com/image2.jpg")));
            assertTrue(deletedImages.stream()
                .anyMatch(img -> img.getUrl().equals("https://example.com/image3.jpg")));
        });
    }
    
    @Test
    public void testAsyncImageCleanupWithException() {
        // Given
        ImageType imageType = ImageType.POST;
        long referenceId = 100L;
        Set<String> usedImageUrls = new HashSet<>();
        
        // S3 서비스가 예외를 던지도록 설정
        doThrow(new RuntimeException("S3 오류 시뮬레이션")).when(s3Service).deleteFile(anyString());
        
        // When (예외가 발생해도 비동기 메서드는 예외를 잡아서 처리)
        imageCleanupService.cleanupUnusedImages(imageType, referenceId, usedImageUrls);
        
        // Then (메인 스레드는 계속 진행되고, 예외는 로그만 남김)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(s3Service, times(3)).deleteFile(anyString());
            // 예외가 발생해도 테스트 실패하지 않음
        });
    }
} 