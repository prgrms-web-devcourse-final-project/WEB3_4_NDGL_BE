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
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.ndgl.spotfinder.domain.image.dto.ImageRequest;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponse;
import com.ndgl.spotfinder.domain.image.dto.UploadCompleteRequest;
import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageType;
import com.ndgl.spotfinder.global.aws.s3.S3Service;
import com.ndgl.spotfinder.global.exception.ServiceException;
import com.ndgl.spotfinder.global.util.Ut;

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
    @DisplayName("이미지 저장 성공")
    public void saveImages_success() {
        // given
        long postId = 10L;
        List<String> imageUrls = Arrays.asList(
            "posts/10/image1.jpg",
            "posts/10/image2.jpg"
        );
        UploadCompleteRequest request = new UploadCompleteRequest(postId, ImageType.POST, imageUrls);

        // Ut.list.hasValue 모킹 - try-with-resources로 mockStatic 사용
        try (MockedStatic<Ut.list> mockedList = mockStatic(Ut.list.class)) {
            mockedList.when(() -> Ut.list.hasValue(imageUrls)).thenReturn(true);

            // when
            imageService.saveImages(request);

            // then
            verify(imageRepository).saveAll(anyList());
        }
    }

    @Test
    @DisplayName("빈 이미지 URL 목록 저장 시 처리")
    public void saveImages_emptyList() {
        // given
        long postId = 10L;
        List<String> emptyUrls = new ArrayList<>();
        UploadCompleteRequest request = new UploadCompleteRequest(postId, ImageType.POST, emptyUrls);

        // Ut.list.hasValue 모킹 - try-with-resources로 mockStatic 사용
        try (MockedStatic<Ut.list> mockedList = mockStatic(Ut.list.class)) {
            mockedList.when(() -> Ut.list.hasValue(emptyUrls)).thenReturn(false);

            // when
            imageService.saveImages(request);

            // then
            verify(imageRepository, never()).saveAll(anyList());
        }
    }

    @Test
    @DisplayName("단일 이미지 삭제 성공")
    public void deleteImageByUrl_success() {
        // given
        String imageUrl = "posts/10/image1.jpg";

        // when
        when(imageRepository.findByUrl(imageUrl)).thenReturn(Optional.of(testImage1));

        imageService.deleteImageByUrl(imageUrl);

        // then
        verify(s3Service).deleteFile(imageUrl);
        verify(imageRepository).delete(testImage1);
    }

    @Test
    @DisplayName("존재하지 않는 이미지 URL 삭제 시도")
    public void deleteImageByUrl_notFound() {
        // given
        String nonExistingUrl = "posts/999/notfound.jpg";

        // when
        when(imageRepository.findByUrl(nonExistingUrl)).thenReturn(Optional.empty());

        imageService.deleteImageByUrl(nonExistingUrl);

        // then
        verify(s3Service).deleteFile(nonExistingUrl);
        verify(imageRepository, never()).delete(any(Image.class));
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