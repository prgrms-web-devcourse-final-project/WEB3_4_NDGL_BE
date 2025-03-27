package com.ndgl.spotfinder.domain.image.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.repository.ImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {
	private final ImageRepository imageRepository;

	// TODO

	public void saveImages(long id, List<String> imageUrls) {

	}

	public void deleteImagesByIdAndUrls(long id, List<String> urls) {

	}

	public long deleteImages(long id) {
		return 0;
	}

	public List<Image> findImagesById(long id) {
		return null;
	}
}
