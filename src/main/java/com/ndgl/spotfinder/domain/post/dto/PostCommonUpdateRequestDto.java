package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

public interface PostCommonUpdateRequestDto {
    String title();
    String content();
    List<HashtagDto> hashtags();
    List<LocationDto> locations();
    String thumbnail();
}
