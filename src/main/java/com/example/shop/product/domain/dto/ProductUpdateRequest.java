package com.example.shop.product.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateRequest {
    private String title;
    private Integer price;
}
