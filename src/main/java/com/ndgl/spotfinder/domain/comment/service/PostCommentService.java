package com.ndgl.spotfinder.domain.comment.service;

import org.springframework.stereotype.Service;

import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostCommentService {
	private final PostCommentRepository postCommentRepository;


}
