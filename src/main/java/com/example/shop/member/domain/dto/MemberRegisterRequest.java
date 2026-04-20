package com.example.shop.member.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRegisterRequest {
    private String username;
    private String password;
    private String displayName;
}
