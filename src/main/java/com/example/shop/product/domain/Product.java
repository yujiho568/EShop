package com.example.shop.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product", indexes = @Index(columnList = "title", name = "titleIndex"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer price;

    @Column
    private String image;

    private Product(String title, Integer price, String image) {
        this.title = title;
        this.price = price;
        this.image = image;
    }

    public static Product create(String title, Integer price, String image) {
        validate(title, price);
        return new Product(title, price, image);
    }

    public void update(String title, Integer price) {
        validate(title, price);
        this.title = title;
        this.price = price;
    }

    public void changeImage(String image) {
        this.image = image;
    }

    private static void validate(String title, Integer price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price must be natural number");
        }
        if (title == null || title.isBlank() || title.length() > 100) {
            throw new IllegalArgumentException("Title must be between 1 and 100");
        }
    }
}
