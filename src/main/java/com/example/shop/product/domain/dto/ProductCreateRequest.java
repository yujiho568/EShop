package com.example.shop.product.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateRequest {
    private String title;
    private Integer price;
    private String image;
}
