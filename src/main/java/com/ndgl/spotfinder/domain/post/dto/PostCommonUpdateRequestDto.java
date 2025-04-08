package com.ndgl.spotfinder.domain.post.dto;

import java.util.List;

import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Location;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.entity.PostStatus;

public interface PostCommonUpdateRequestDto {
	String title();

	String content();

	List<HashtagDto> hashtags();

	List<LocationDto> locations();

	String thumbnail();

	default Post toUpdatedPost(Post post, boolean temp) {
		post.setTitle(title());
		post.setContent(content());
		post.setThumbnail(thumbnail());
		post.setStatus(temp ? PostStatus.TEMP : PostStatus.PUBLIC);

		List<Hashtag> newHashtags = hashtags()
			.stream()
			.map(HashtagDto::toHashtag)
			.toList();
		post.updateHashtags(newHashtags);

		List<Location> newLocations = locations()
			.stream()
			.map(LocationDto::toLocation)
			.toList();
		post.updateLocations(newLocations);

		return post;
	}
}
