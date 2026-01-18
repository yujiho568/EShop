package com.example.shop.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

//interface를 만들어도 class ItemRepository도 생성해줌 - DB 입출력 함수 잔뜩 들어있음

public interface ItemRepository extends JpaRepository<Item, Integer> {
    Page<Item> findPageBy(Pageable page);
    List<Item> findAllByTitleContains(String title);
    @Query(value = "select * from shop.item where id = ?1", nativeQuery = true)
    Item rawQuery1(Long id);

    @Query(value = "SELECT * FROM shop.item WHERE MATCH(title) AGAINST(?1)",  nativeQuery = true)
    Page<Item> fullTextSearch(String text, Pageable pageable);
}

