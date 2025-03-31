package com.ndgl.spotfinder.domain.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndgl.spotfinder.domain.image.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByPostId(long postId);
    
    List<Image> findAllByPostId(Long postId);
    
    void deleteAllByPostId(Long postId);
}
