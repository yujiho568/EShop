package com.example.shop.sales.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private Integer price;
    private Integer count;
    private Long memberId;

    @CreationTimestamp
    private LocalDateTime created;

    private Sales(String itemName, Integer price, Integer count, Long memberId) {
        this.itemName = itemName;
        this.price = price;
        this.count = count;
        this.memberId = memberId;
    }

    public static Sales create(String itemName, Integer price, Integer count, Long memberId) {
        return new Sales(itemName, price, count, memberId);
    }
}
