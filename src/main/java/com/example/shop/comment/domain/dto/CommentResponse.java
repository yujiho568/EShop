package com.example.shop.comment.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String username;
    private String content;
}
