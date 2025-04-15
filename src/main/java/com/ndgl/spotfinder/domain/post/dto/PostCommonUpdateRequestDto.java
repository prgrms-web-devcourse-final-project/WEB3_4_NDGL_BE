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

	default Post toUpdatedPost(Post post, PostStatus postStatus) {
		post.setTitle(title());
		post.setContent(content());
		post.setThumbnail(thumbnail());
		post.changeStatus(postStatus);

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
