package com.example.shop.product.repository;

import com.example.shop.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findPageBy(Pageable page);

    List<Product> findAllByTitleContains(String title);

    @Query(value = "select * from product where id = ?1", nativeQuery = true)
    Product rawQuery1(Long id);

    @Query(value = "SELECT * FROM product WHERE MATCH(title) AGAINST(?1)", nativeQuery = true)
    Page<Product> fullTextSearch(String text, Pageable pageable);
}
