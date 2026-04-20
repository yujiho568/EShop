package com.example.shop.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String displayName;

    private Member(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    public static Member create(String username, String encodedPassword, String displayName) {
        return new Member(username, encodedPassword, displayName);
    }

    public void changeDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
