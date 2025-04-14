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
import com.ndgl.spotfinder.domain.image.type.ImageUsage;
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
			.imageUsage(ImageUsage.POST)
			.referenceId(100L)
			.url("https://example.com/image1.jpg")
			.build();

		Image image2 = Image.builder()
			.id(2L)
			.imageUsage(ImageUsage.POST)
			.referenceId(100L)
			.url("https://example.com/image2.jpg")
			.build();

		Image image3 = Image.builder()
			.id(3L)
			.imageUsage(ImageUsage.POST)
			.referenceId(100L)
			.url("https://example.com/image3.jpg")
			.build();

		mockImages = Arrays.asList(image1, image2, image3);

		// Mock 설정
		when(imageRepository.findByImageUsageAndReferenceId(ImageUsage.POST, 100L))
			.thenReturn(mockImages);
	}

	@Test
	public void testAsyncImageCleanupWithException() {
		// Given
		ImageUsage imageUsage = ImageUsage.POST;
		long referenceId = 100L;
		Set<String> usedImageUrls = new HashSet<>();

		// S3 서비스가 예외를 던지도록 설정
		doThrow(new RuntimeException("S3 오류 시뮬레이션")).when(s3Service).deleteFile(anyString());

		// When (예외가 발생해도 비동기 메서드는 예외를 잡아서 처리)
		imageCleanupService.cleanupUnusedImages(imageUsage, referenceId, usedImageUrls, null);

		// Then (메인 스레드는 계속 진행되고, 예외는 로그만 남김)
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(s3Service, times(3)).deleteFile(anyString());
			// 예외가 발생해도 테스트 실패하지 않음
		});
	}

	@Test
	public void testCleanupUnusedImages() {
		// Given
		ImageUsage imageUsage = ImageUsage.POST;
		long referenceId = 100L;
		
		// 사용 중인 이미지가 없음 (content에 이미지 없음)
		Set<String> usedImageUrls = new HashSet<>();
		
		// 썸네일은 image2.jpg로 설정
		String thumbnailUrl = "https://example.com/image2.jpg";
		
		final AtomicBoolean asyncExecuted = new AtomicBoolean(false);
		
		doAnswer(invocation -> {
			asyncExecuted.set(true);
			return null;
		}).when(s3Service).deleteFile(anyString());
		
		// When
		imageCleanupService.cleanupUnusedImages(imageUsage, referenceId, usedImageUrls, thumbnailUrl);
		
		// Then
		await().atMost(5, TimeUnit.SECONDS).untilTrue(asyncExecuted);
		
		// 썸네일(image2.jpg)은 삭제되지 않고, 나머지 이미지(image1.jpg, image3.jpg)만 삭제되어야 함
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(s3Service, times(2)).deleteFile(anyString());
			verify(imageRepository, times(2)).delete(imageCaptor.capture());
			
			List<Image> deletedImages = imageCaptor.getAllValues();
			assertEquals(2, deletedImages.size());
			
			// 썸네일을 제외한 이미지만 삭제되었는지 확인
			assertTrue(deletedImages.stream()
				.anyMatch(img -> img.getUrl().equals("https://example.com/image1.jpg")));
			assertTrue(deletedImages.stream()
				.anyMatch(img -> img.getUrl().equals("https://example.com/image3.jpg")));
			
			// 썸네일은 삭제되지 않았는지 확인
			assertFalse(deletedImages.stream()
				.anyMatch(img -> img.getUrl().equals("https://example.com/image2.jpg")));
		});
	}

	@Test
	public void testCleanupUnusedImagesWithNullThumbnail() {
		// Given
		ImageUsage imageUsage = ImageUsage.POST;
		long referenceId = 100L;
		
		// 사용 중인 이미지 - image1만 컨텐츠에서 사용 중
		Set<String> usedImageUrls = new HashSet<>();
		usedImageUrls.add("https://example.com/image1.jpg");
		
		// 썸네일이 null인 경우 (설정되지 않은 경우)
		String thumbnailUrl = null;
		
		final AtomicBoolean asyncExecuted = new AtomicBoolean(false);
		
		doAnswer(invocation -> {
			asyncExecuted.set(true);
			return null;
		}).when(s3Service).deleteFile(anyString());
		
		// When
		imageCleanupService.cleanupUnusedImages(imageUsage, referenceId, usedImageUrls, thumbnailUrl);
		
		// Then
		await().atMost(5, TimeUnit.SECONDS).untilTrue(asyncExecuted);
		
		// image1만 보존되고 나머지는 삭제되어야 함
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(s3Service, times(2)).deleteFile(anyString());
			verify(imageRepository, times(2)).delete(imageCaptor.capture());
			
			List<Image> deletedImages = imageCaptor.getAllValues();
			assertEquals(2, deletedImages.size());
			
			// 사용 중인 이미지(image1)를 제외한 이미지만 삭제되었는지 확인
			assertTrue(deletedImages.stream()
				.anyMatch(img -> img.getUrl().equals("https://example.com/image2.jpg")));
			assertTrue(deletedImages.stream()
				.anyMatch(img -> img.getUrl().equals("https://example.com/image3.jpg")));
		});
	}

	@Test
	public void testCleanupUnusedImagesWithContentAndThumbnail() {
		// Given
		ImageUsage imageUsage = ImageUsage.POST;
		long referenceId = 100L;
		
		// 사용 중인 이미지 - image1은 컨텐츠에서 사용 중
		Set<String> usedImageUrls = new HashSet<>();
		usedImageUrls.add("https://example.com/image1.jpg");
		
		// 썸네일은 image3으로 설정
		String thumbnailUrl = "https://example.com/image3.jpg";
		
		final AtomicBoolean asyncExecuted = new AtomicBoolean(false);
		
		doAnswer(invocation -> {
			asyncExecuted.set(true);
			return null;
		}).when(s3Service).deleteFile(anyString());
		
		// When
		imageCleanupService.cleanupUnusedImages(imageUsage, referenceId, usedImageUrls, thumbnailUrl);
		
		// Then
		await().atMost(5, TimeUnit.SECONDS).untilTrue(asyncExecuted);
		
		// image1(컨텐츠), image3(썸네일)은 보존되고 image2만 삭제되어야 함
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			verify(s3Service, times(1)).deleteFile(anyString());
			verify(imageRepository, times(1)).delete(imageCaptor.capture());
			
			Image deletedImage = imageCaptor.getValue();
			assertEquals("https://example.com/image2.jpg", deletedImage.getUrl());
		});
	}
} 