package com.example.shop.member.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberProfileResponse {
    private String username;
    private String displayName;
}
