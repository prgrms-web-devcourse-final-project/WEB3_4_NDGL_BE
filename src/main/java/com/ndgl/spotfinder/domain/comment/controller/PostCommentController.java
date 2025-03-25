package com.ndgl.spotfinder.domain.comment.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ndgl.spotfinder.domain.comment.service.PostCommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/post/{id}/comment")
@RequiredArgsConstructor
public class PostCommentController {
	private final PostCommentService postCommentService;


}
