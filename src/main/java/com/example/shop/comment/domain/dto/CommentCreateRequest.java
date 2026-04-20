package com.example.shop.comment.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {
    private String content;
    private Long productId;
}
