package com.example.shop.comment.domain;

import com.example.shop.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Comment(Product product, String username, String content) {
        this.product = product;
        this.username = username;
        this.content = content;
    }

    public static Comment create(Product product, String username, String content) {
        if (product == null) {
            throw new IllegalArgumentException("product is required");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        return new Comment(product, username, content);
    }

    public void changeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        this.content = content;
    }
}
