package com.example.shop.product.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {
    private Integer id;
    private String title;
    private Integer price;
    private String image;
}
