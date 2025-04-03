package com.ndgl.spotfinder.domain.image.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.image.entity.Image;
import com.ndgl.spotfinder.domain.image.type.ImageType;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
	List<Image> findByImageTypeAndReferenceId(ImageType imageType, Long referenceId);

	void deleteAllByImageTypeAndReferenceId(ImageType imageType, Long postId);

	Optional<Image> findByUrl(String url);
}
