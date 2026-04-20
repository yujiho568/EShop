package com.example.shop.product.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductListResponse {
    private List<ProductResponse> products;
    private int currentPage;
    private int totalPages;
    private String searchText;
}
