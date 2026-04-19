package com.example.shop.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    public void registerMember(Member member) throws Exception {
        String username = Objects.requireNonNullElse(member.getUsername(), "").trim();
        String password = Objects.requireNonNullElse(member.getPassword(), "").trim();
        String displayName = Objects.requireNonNullElse(member.getDisplayName(), "").trim();

        if (username.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
            throw new Exception("all fields are required");
        }
        if(memberRepository.findByUsername(username).isPresent()){
            throw new Exception("already exist");
        }
        if(memberRepository.findByDisplayName(displayName).isPresent()){
            throw new Exception("display name already exist");
        }
        if(username.length() < 4 || password.length() < 8)
            throw new Exception("too short");
        member.setUsername(username);
        member.setDisplayName(displayName);
        member.setPassword(passwordEncoder.encode(password));
        memberRepository.save(member);
    }


}
