package com.example.shop.comment.service;

import com.example.shop.comment.domain.Comment;
import com.example.shop.comment.domain.dto.CommentCreateRequest;
import com.example.shop.comment.domain.dto.CommentResponse;
import com.example.shop.comment.repository.CommentRepository;
import com.example.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;

    public void save(CommentCreateRequest request, String username) {
        var product = productRepository.findById(request.getProductId().intValue())
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        Comment comment = Comment.create(product, username, request.getContent());
        commentRepository.save(comment);
    }

    public List<CommentResponse> getComments(Long productId) {
        return commentRepository.findByProductId(productId).stream()
                .map(comment -> new CommentResponse(comment.getId(), comment.getUsername(), comment.getContent()))
                .toList();
    }
}
