package com.example.shop.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "item", indexes=@Index(columnList = "title", name="titleIndex"))
@ToString
@Getter
@Setter
public class Item {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false, unique=true)
    private String title;
    @Column(nullable = false, unique=true)
    private Integer price;
    @Column
    private String image;
}
