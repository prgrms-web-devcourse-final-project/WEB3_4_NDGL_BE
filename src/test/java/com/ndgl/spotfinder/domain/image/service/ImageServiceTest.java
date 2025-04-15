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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.ndgl.spotfinder.domain.image.dto.ImageUrlRequestDto;
import com.ndgl.spotfinder.domain.image.dto.PresignedUrlsResponseDto;
import com.ndgl.spotfinder.domain.image.dto.UploadCompleteRequestDto;
import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;
import com.ndgl.spotfinder.domain.image.type.ImageUsage;
import com.ndgl.spotfinder.global.aws.s3.S3Service;
import com.ndgl.spotfinder.global.common.util.CommonUtil;
import com.ndgl.spotfinder.global.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
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
		.imageUsage(ImageUsage.POST)
		.referenceId(10L)
		.url("posts/10/image1.jpg")
		.build();

	private final Image testImage2 = Image.builder()
		.id(2L)
		.imageUsage(ImageUsage.POST)
		.referenceId(10L)
		.url("posts/10/image2.jpg")
		.build();

	@Test
	@DisplayName("Presigned URL 생성 - 정상")
	void createImage_success() throws Exception {
		// given
		long postId = 10L;
		List<String> extensions = Arrays.asList("jpg", "png");
		ImageUrlRequestDto request = new ImageUrlRequestDto(postId, ImageUsage.POST, extensions);

		List<URL> mockUrls = new ArrayList<>();
		mockUrls.add(new URL("https://example.com/image1.jpg"));
		mockUrls.add(new URL("https://example.com/image2.png"));

		// when
		when(s3Service.generatePresignedUrls(ImageUsage.POST, postId, extensions))
			.thenReturn(mockUrls);

		PresignedUrlsResponseDto response = imageService.createImage(request);

		// then
		assertNotNull(response);
		assertEquals(2, response.presignedUrls().size());
		verify(s3Service).generatePresignedUrls(ImageUsage.POST, postId, extensions);
	}

	@Test
	@DisplayName("Presigned URL 생성 - DB 제약조건 위반")
	void createImage_dbConstraintViolation_fail() throws Exception {
		// given
		long postId = 10L;
		List<String> extensions = Arrays.asList("jpg", "png");
		ImageUrlRequestDto request = new ImageUrlRequestDto(postId, ImageUsage.POST, extensions);

		// when
		when(s3Service.generatePresignedUrls(ImageUsage.POST, postId, extensions))
			.thenThrow(new DataIntegrityViolationException("DB 제약조건 위반"));

		// then
		assertThrows(ServiceException.class, () -> imageService.createImage(request));
	}

	@Test
	@DisplayName("이미지 저장 - 정상")
	void saveImages_success() {
		// given
		long postId = 10L;
		List<String> imageUrls = Arrays.asList(
			"posts/10/image1.jpg",
			"posts/10/image2.jpg"
		);
		UploadCompleteRequestDto request = new UploadCompleteRequestDto(postId, ImageUsage.POST, imageUrls);

		// Ut.list.hasValue 모킹 - try-with-resources로 mockStatic 사용
		try (MockedStatic<CommonUtil.list> mockedList = mockStatic(CommonUtil.list.class)) {
			mockedList.when(() -> CommonUtil.list.hasValue(imageUrls)).thenReturn(true);

			// when
			imageService.saveImages(request);

			// then
			verify(imageRepository).saveAll(anyList());
		}
	}

	@Test
	@DisplayName("이미지 저장 - 빈 목록")
	void saveImages_emptyList_success() {
		// given
		long postId = 10L;
		List<String> emptyUrls = new ArrayList<>();
		UploadCompleteRequestDto request = new UploadCompleteRequestDto(postId, ImageUsage.POST, emptyUrls);

		// Ut.list.hasValue 모킹 - try-with-resources로 mockStatic 사용
		try (MockedStatic<CommonUtil.list> mockedList = mockStatic(CommonUtil.list.class)) {
			mockedList.when(() -> CommonUtil.list.hasValue(emptyUrls)).thenReturn(false);

			// when
			imageService.saveImages(request);

			// then
			verify(imageRepository, never()).saveAll(anyList());
		}
	}

	@Test
	@DisplayName("이미지 URL 삭제 - 정상")
	void deleteImageByUrl_success() {
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
	@DisplayName("이미지 URL 삭제 - 존재하지 않음")
	void deleteImageByUrl_notFound_success() {
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
	@DisplayName("게시물 이미지 전체 삭제 - 정상")
	void deletePostWithAllImages_success() {
		// given
		long postId = 10L;

		// when
		imageService.deletePostWithAllImages(ImageUsage.POST, postId);

		// then
		verify(imageRepository).deleteAllByImageUsageAndReferenceId(ImageUsage.POST, postId);
	}
}